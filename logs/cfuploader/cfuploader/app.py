#!/usr/bin/env python

from multiprocessing import Process
import swiftclient
import threading
import traceback
import operator
import requests
import datetime
import MySQLdb
import zipfile
import hashlib
import string
import thread
import errno
import Queue
import time
import json
import sys
import os
import re


class Uploader(object):

    def __init__(self):
        self.timestamp = time.time() - 120.00
        self.n_workers = 1
        #self.n_workers = 1cfg.conf['n_workers']

    def start_worker(self, zip_container):
        th = threading.Thread(target=worker_thread, args=(zip_container,))
        th.setDaemon(True)
        log("starting thread %s\n", zip_container)
        th.start()

    def main_loop(self):
        while True:
            time.sleep(0.25)
            now = time.time()
            if self.timestamp + 120.0 < now:
                log("scanning zips\n")
                try:
                    zip_containers = get_container_zips()
                    log("zips scanned\n")
                    l.acquire()
                    for zc in zip_containers:
                        (aid, lid, hl, src_file, cnt, remote_file) = zc
                        if zc not in upload_files:
                            all_files.add(zc)
                    l.release()
                except:
                    log("Error scanning zip directory: %s\n", excuse())
                    l.release()
                self.timestamp = time.time()
            #Drain Queue
            l.acquire()
            while not q.empty():
                zc = q.get()
                all_files.discard(zc)
                upload_files.discard(zc)
                printf("removing %s from upload status\n", zc[3])
            nready = self.n_workers - len(upload_files)
            sendable_files = list(all_files - upload_files)
            sort_container_zips(sendable_files)
            while len(sendable_files) > 0 and nready > 0:
                zc = sendable_files.pop(0)
                log("spawning thread to send %s nread = %i\n", zc, nready)
                nready -= 1
                upload_files.add(zc)
                self.start_worker(zc)
            l.release()


class Config(object):
    def __init__(self, conf_file="/etc/cfuploader/cfuploader.json"):
        conf = self.load_json(conf_file)
        self.auth_url = conf['auth_url']
        self.auth_user = conf['auth_user']
        if self.auth_url[-1] == "/":
            self.auth_url = self.auth_url[:-1]
        self.auth_passwd = conf['auth_passwd']
        self.db = conf['db']
        self.run_file = conf["run_file"]
        self.log_file = conf["log_file"]
        self.conf = conf

    def load_json(self, pathIn):
        return json.loads(open(os.path.expanduser(pathIn),"r").read())

    def save_json(self, pathOut, obj):
        open(os.path.expanduser(pathOut),"w").write(json.dumps(obj,indent=2))


class Auth(object):
    def __init__(self, conf=None):
        if conf is None:
            conf = Config()
        self.conf = conf
        self.auth_url = conf.auth_url
        self.auth_user = conf.auth_user
        self.auth_passwd = conf.auth_passwd
        self.token = None
        self.expires = None

    def prep_headers(self):
        return {'content-type':'application/json', 'accept':'application/json',
               'x-auth-token': self.token}

    def get_admin_token(self):
        up = {'username': self.auth_user, 'password': self.auth_passwd}
        payload={'auth':{'passwordCredentials':up}}
        hdr = self.prep_headers()
        uri = self.auth_url + "/v2.0/tokens"
        r = requests.post(uri, headers=hdr, data=json.dumps(payload))
        obj = json.loads(r.text)
        self.token = obj["access"]["token"]["id"]
        self.expires = obj["access"]["token"]["expires"]
        return {"token": self.token, "expires": self.expires}

    def get_endpoints(self, domain_id):
        uri = self.auth_url + "/v2.0/RAX-AUTH/domains/%s/endpoints"%domain_id
        hdr = self.prep_headers()
        r = requests.get(uri, headers=hdr)
        out = json.loads(r.text)
        if len(out['endpoints']) <= 0:
            log("Error no endpoints found for %i\n", domain_id)
        return out

    def get_primary_user(self, domain_id):
        uri = self.auth_url + "/v2.0/RAX-AUTH/domains/%s/users"%(domain_id)
        r = requests.get(uri, headers=self.prep_headers())
        resp = json.loads(r.text)
        username = resp['users'][0]['username']
        region = resp['users'][0]['RAX-AUTH:defaultRegion']
        return {'user': username, 'region': region}

    def impersonate_user(self, username):
        uri = self.auth_url + "/v2.0/RAX-AUTH/impersonation-tokens"
        imp  = {'user':{'username': username}, 'expire-in-seconds': 3600}
        obj = {'RAX-AUTH:impersonation': imp}
        json_data = json.dumps(obj)
        r = requests.post(uri,headers=self.prep_headers(), data=json_data)
        obj = json.loads(r.text)
        return obj['access']['token']['id']

    def get_token_and_endpoint(self, domain_id):
        pu = self.get_primary_user(domain_id)
        token = self.impersonate_user(pu['user'])
        eps = self.get_endpoints(domain_id)
        cf_eps = [e for e in eps['endpoints'] if e['type'] == 'object-store']
        found_endpoints = []
        cf_endpoint = None
        for ep in cf_eps:
            if ep['region'] == pu['region']:
                cf_endpoint = ep['publicURL']
                break
        return {'token': token, 'cf_endpoint': cf_endpoint}

    def get_token_and_endpoint(auth, domain_id):
        pu = auth.get_primary_user(domain_id)
        token = auth.impersonate_user(pu['user'])
        eps = auth.get_endpoints(domain_id)
        cf_eps = [e for e in eps['endpoints'] if e['type'] == 'object-store']
        cf_endpoint = None
        for ep in cf_eps:
            if ep['region'] == pu['region']:
                cf_endpoint = ep['publicURL']
                break
        if cf_endpoint == None:
            printf("ERROR EMPTY ENDPOINT %s\n", eps)
        return {'token': token, 'cf_endpoint': cf_endpoint}


class CloudFiles(object):
    def __init__(self, tok_endpoint):
        self.token = tok_endpoint['token']
        self.end_point = tok_endpoint['cf_endpoint']
        self.con = swiftclient.client.Connection(retries=0,
                                                 preauthurl=self.end_point,
                                                 preauthtoken=self.token,
                                                 snet=True,
                                                 ssl_compression=False)

    def list_containers(self):
        resp = self.con.get_account(full_listing=True)
        return resp[1]

    def create_container(self, name):
        self.con.put_container(name, {})

    def upload_file(self, src_name, cnt_name, remote_name):
        fp = open(os.path.expanduser(src_name), "r")
        resp = self.con.put_object(cnt_name, remote_name, fp, chunk_size=512*1024)
        fp.close()
        return resp

    def upload_zip(self, zfile):
        (aid, lid, hl, local_name, cnt, remote_name) = zfile
        self.create_container(cnt)
        resp = self.upload_file(local_name, cnt, remote_name)
        return resp



class DbHelper(object):
    def __init__(self, conf=None):
        if conf == None:
            conf = Config()
        self.db = conf.db

    def get_lb_map(self):
        q  = "select account_id, id, name from loadbalancer"
        con = MySQLdb.connect(**self.db)
        cur = con.cursor(MySQLdb.cursors.DictCursor)
        cur.execute(q)
        rows = cur.fetchall()
        cur.close()
        con.close()
        rows_out = {}
        for row in rows:
            aid = row['account_id']
            lid = row['id']
            name = row['name'].replace(" ", "_").replace("/","_")
            rows_out[lid] = (aid, lid, name)
        return rows_out

    def get_lb_ids(self, aid_str):
        aid = int(aid_str)
        con = MySQLdb.connect(**self.db)
        cur = con.cursor(MySQLdb.cursors.DictCursor)
        cur.execute(account_lb_query,(aid,))
        rows = cur.fetchall()
        cur.close()
        con.close()
        rows_out = {}
        for row in rows:
            aid = row['account_id']
            lid = row['id']
            name = row['name'].replace(" ", "_").replace("/","_")
            rows_out[lid] = (aid, lid, name)
        return rows_out


def excuse():
    except_message = traceback.format_exc()
    stack_message  = traceback.format_stack()
    return except_message + " " + str(stack_message)


def mkdirs_p(file_path):
    try:
        os.makedirs(file_path)
    except OSError as e:
        if e.errno == errno.EEXIST and os.path.isdir(file_path):
            pass
        else:
            raise


def set_local_file(aid,lid,dt):
    hl = datetime_to_hourlong(dt)
    tail = "access_log_%i_%i_%i.log.zip"%(aid,lid,hl)
    return os.path.join(incoming, tail)


def datetime_to_hourlong(dt):
    hl =  dt.year * 1000000 + dt.month * 10000 + dt.day * 100 + dt.hour * 1
    return hl


def test_re(pattern,text):
    pattern_re = re.compile(pattern)
    m = pattern_re.match(text)
    if m:
        return m.groups()
    else:
        return False


def get_archive_file_path(zip_container):
    (aid, lid, hl, local_file, cnt, remote_file) = zip_container
    file_name = os.path.split(local_file)[1]
    dir_name = os.path.join(archive, str(hl), str(aid))
    return (dir_name, file_name)


def get_formatted_file_date(hl):
    hs = str(hl)
    fd = []
    for i in xrange(0,len(hs)):
        fd.append(hs[i])
        if i == 3 or i == 5 or i == 7:
            fd.append('-')
    fd.append(":00")
    return string.join(fd, "")


def get_month_year(hl):
    month = (hl/10000)%100
    year = hl/1000000
    return "%s_%i"%(months[month], year)


def parse_zip_name(zip_path):
    db = DbHelper()

    m = zip_re.match(zip_path)
    if m:
        zip_file = m.group(1)
        aid = int(m.group(2))
        lid = int(m.group(3))
        hl = int(m.group(4))
        return (zip_file, aid, lid, hl, zip_path)
    return None


def scan_zip_files(file_path):
    zip_files = []
    for (root, dirnames, file_names) in os.walk(file_path):
        for file_name in file_names:
            full_path = os.path.join(root, file_name)
            pzn = parse_zip_name(full_path)
            if(pzn):
                (zip_file, aid, lid, hl, zip_path) = pzn
                zpath = os.path.expanduser(file_path)
                full_path = os.path.join(zpath, zip_file)
                zip_files.append((aid, lid, hl, full_path))
    return zip_files


def sort_container_zips(czs):
    czs.sort(key=operator.itemgetter(2, 0, 1),reverse=True)


def get_container_zips():
    db = DbHelper()
    czs = []
    lb_map = db.get_lb_map()
    zfiles = scan_zip_files(incoming)
    for (aid, lid, hl, zip_file) in zfiles:
        if lid not in lb_map:
            log("lid %i not found in database skipping. Not even ",lid)
            log("sure what account is was under\n")
            continue
        (j1, j2, name) = lb_map[lid] #Throw away j1 and j2
        cnt = get_container_name(lid, name, hl)
        zip = get_remote_file_name(lid, name, hl)
        czs.append( (aid,lid, hl, zip_file, cnt, zip) )
        sort_container_zips(czs)
    return czs


def get_remote_file_name(lid, name, hl):
    dt_str = get_formatted_file_date(hl)
    rfn = "lb_%i_%s_%s.zip"%(lid, name, dt_str)
    return rfn.replace(" ","_").replace("/","_")


def get_container_name(lid, name, hl):
    my = get_month_year(hl)
    cnt_name = "lb_%i_%s_%s"%(lid, name, my)
    return cnt_name.replace(" ","_").replace("/","-")


def hourlong_to_datetime(hl):
    hour = hl % 100
    hl /= 100
    day = hl % 100
    hl /= 100
    month = hl % 100
    hl /= 100
    year = hl
    dt = datetime.datetime(year, month, day, hour)
    return dt


def lpad(n, digits):
    out = str(n)
    pad = "0"
    return pad*(digits-len(out)) + out


def datetime_to_formatted_time(dt=None):
    if dt is None:
        dt = datetime.datetime.now()
    year = lpad(dt.year,4)
    month = lpad(dt.month,2)
    day = lpad(dt.day,2)
    hour = lpad(dt.hour,2)
    mnt = lpad(dt.minute,2)
    sec = lpad(dt.second,2)
    ms = lpad(dt.microsecond/1000,3)
    return "%s-%s-%s %s:%s:%s.%s"%(year,month,day,hour,mnt,sec,ms)


def md5sum_and_size(file_name,block_size = 64*1024):
    md5 = hashlib.md5()
    full_path = os.path.expanduser(file_name)
    fp = open(full_path,"rb", block_size)
    total_bytes = 0
    while True:
        data = fp.read(block_size)
        nbytes = len(data)
        total_bytes += nbytes
        if nbytes == 0:
            break
        md5.update(data)
    return (md5.hexdigest(), total_bytes)


#Used for testing
def create_fake_zips(account_id, n_hours):
    lbs = []
    db = DbHelper()
    lbs = db.get_lb_ids(account_id).values()
    now = datetime.datetime.now()
    for i in xrange(0,n_hours):
        dt = now + datetime.timedelta(hours=-i)
        for lb in lbs:
            (aid, lid, name) = lb
            printf("writing %s %s %s to ", aid, lid, name)
            file_name = set_local_file(aid, lid, dt)
            mkdirs_p(incoming)
            full_path = os.path.join(incoming,file_name)
            printf("-> %s\n", full_path)
            zf = zipfile.ZipFile(file_name, mode="w",
                            compression=zipfile.ZIP_DEFLATED)
            str_list = []
            for i in xrange(0,4096):
                str_list.append("Line %i\n"%i)
            data = string.join(str_list,"")
            zf.writestr('test_log.txt', data)
            zf.close()


def log(format, *args):
    printf("[%5i][%s] "%(os.getpid(), datetime_to_formatted_time()))
    printf(format, *args)
    sys.stdout.flush()


def fprintf(fp, format, *args): fp.write(format%args)


def printf(format, *args): sys.stdout.write(format%args)

def worker_process(zip_container, exp_md5):
    (aid, lid, hl, local_file, cnt, remote_file) = zip_container
    auth = Auth()
    auth.get_admin_token()
    t = auth.get_token_and_endpoint(aid)
    try:
        cf = CloudFiles(t)
        cf.create_container(cnt)
    except:
        log("Error could not send file %s %s: %s\n", zip_container, t, excuse())
        return
    act_md5 = cf.upload_file(local_file, cnt, remote_file)
    if act_md5 == exp_md5:
        (archive_dir, archive_file) = get_archive_file_path(zip_container)
        mkdirs_p(archive_dir)
        archive_path = os.path.join(archive_dir, archive_file)
        os.rename(local_file, archive_path)


def worker_thread(zip_container):
    (aid,lid, hl, src_file, cnt, dst_file) = zip_container
    log("worker thread %s\n", zip_container)
    try:
        (md5, fsize) = md5sum_and_size(src_file, block_size=1024*1024)
        log("%s: %s %i uploading now\n", src_file, md5, fsize)
        p = Process(target=worker_process, args=(zip_container, md5))
        p.start()
        p.join()
        log("finished sending %i %7i %7i log file.\n", hl, aid, lid)
    except:
        log("error sending log file %i %7i %7i: %s\n", hl, aid, lid, excuse())
    l.acquire()
    q.put(zip_container)
    l.release()

zip_re = re.compile(".*/(access_log_([0-9]+)_([0-9]+)_([0-9]{10})\.log.zip)")

incoming = "/var/log/zxtm/processed"
archive = "/var/log/zxtm/archive"
months = [   '', 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
              'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']

l = thread.allocate_lock()
q = Queue.Queue()

all_files = set()
upload_files = set()

account_lb_query = """
select account_id, id, name from loadbalancer
where account_id = %s and status = 'ACTIVE'
"""

cfg = Config()
