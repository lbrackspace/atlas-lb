
import Crypto.Hash.MD5
from clients import *
import traceback
import operator
import datetime
import zipfile
import clients
import string
import errno
import sys
import os
import re

zip_re = re.compile(".*/(([0-9]+)/access_log_([0-9]+)_([0-9]{10})\.log.zip)")

lp = open("/var/log/cfuploader.log","wa")

incoming = "/var/log/zxtm/processed"
archive = "/var/log/zxtm/archive"
months = [   '', 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
              'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']

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
    tail = "%i/access_log_%i_%i.log.zip"%(aid,lid,hl)
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
        if i==3 or i==5 or i==7:
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


def get_container_zip(db, obj):
    (aid, lid, hl, zip_file) = obj
    (aid, foo, name) = db.get_lb_name(lid)
    cnt = get_container_name(lid, name, hl)
    zip = get_remote_file_name(lid, name, hl)
    return (cnt, zip)

def sort_container_zips(czs):
    czs.sort(key=operator.itemgetter(2, 0, 1))


def get_container_zips():
    db = clients.DbHelper()
    czs = []
    lb_map = db.get_lb_map()
    zfiles = scan_zip_files(incoming)
    for (aid, lid, hl, zip_file) in zfiles:
        if lid not in lb_map:
            log("lid %i not found in database skipping\n", lid)
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


def md5sum_and_size(file_name,block_size = 64*1024):
    md5 = Crypto.Hash.MD5.new()
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


def create_fake_zips(account_id, n_hours):
    lbs = []
    db = clients.DbHelper()
    lbs = db.get_lb_ids(account_id).values()
    now = datetime.datetime.now()
    for i in xrange(0,n_hours):
        dt = now + datetime.timedelta(hours=-i)
        for lb in lbs:
            (aid, lid, name) = lb
            printf("writing %s %s %s\n", aid, lid, name)
            file_name = set_local_file(aid, lid, dt)
            dir_name = os.path.split(file_name)[0]
            mkdirs_p(dir_name)
            zf = zipfile.ZipFile(file_name, mode="w",
                            compression=zipfile.ZIP_DEFLATED)
            str_list = []
            for i in xrange(0,4096):
                str_list.append("Line %i\n"%i)
            data = string.join(str_list,"")
            zf.writestr('test_log.txt', data)
            zf.close()

def printf(format,*args): sys.stdout.write(format%args)


def log(format, *args):
    lp.write(format%args)
    lp.flush()

def fprintf(fp,format,*args): fp.write(format%args)
