import swiftclient
import datetime
import requests
import MySQLdb
import zipfile
import string
import json
import os

from cfuploader import utils

account_lb_query = """
select account_id, id, name from loadbalancer
where account_id = %s and status = 'ACTIVE'
"""

class Auth(object):
    def __init__(self, conf=None):
        if conf is None:
            conf = utils.Config()
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
        return json.loads(r.text)

    def get_primary_user(self, domain_id):
        uri = self.auth_url + "/v2.0/RAX-AUTH/domains/%s/users"%(domain_id)
        r = requests.get(uri, headers=self.prep_headers())
        resp = json.loads(r.text)
        username = resp['users'][0]['username']
        region = resp['users'][0]['RAX-AUTH:defaultRegion']
        return {'user': username, 'region': region}

    def get_endpoints_by_token(self, token):
        uri = self.auth_url + "/v2.0/tokens/%s/endpoints"%(token)
        r = requests.get(uri, headers=self.prep_headers())
        resp = json.loads(r.text)
        return resp

    def impersonate_user(self, username):
        uri = self.auth_url + "/v2.0/RAX-AUTH/impersonation-tokens"
        imp  = {'user': {'username': username}, 'expire-in-seconds': 3600}
        obj = {'RAX-AUTH:impersonation': imp}
        json_data = json.dumps(obj)
        r = requests.post(uri, headers=self.prep_headers(), data=json_data)
        obj = json.loads(r.text)
        return obj['access']['token']['id']

    def get_token_and_endpoint(self, domain_id):
        pu = self.get_primary_user(domain_id)
        token = self.impersonate_user(pu['user'])
        # eps = self.get_endpoints(domain_id)
        eps = self.get_endpoints_by_token(token)
        cf_eps = [e for e in eps['endpoints'] if e['type'] == 'object-store']
        found_endpoints = []
        cf_endpoint = None
        for ep in cf_eps:
            if ep['region'] == pu['region']:
                cf_endpoint = ep['publicURL']
                break

        out = {'token': token, 'cf_endpoint': cf_endpoint}
        if cf_endpoint is None:
            msg = ''.join([
                "Error coulden't get endpoint for aid=%s pu=%s ",
                "token=%s eps=%s\n"]) % (domain_id, pu, token, eps)
            raise Exception(msg)
        return out


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
        with open(os.path.expanduser(src_name), "r") as fp:
            resp = self.con.put_object(cnt_name, remote_name, fp,
                                       chunk_size=512*1024)
        return resp

    def upload_zip(self, zfile):
        (aid, lid, hl, local_name, cnt, remote_name) = zfile
        self.create_container(cnt)
        resp = self.upload_file(local_name, cnt, remote_name)
        return resp



class DbHelper(object):
    def __init__(self, conf=None):
        if conf == None:
            conf = utils.Config()
        self.db = conf.db

    def get_lb_map(self):
        q = "select account_id, id, name from loadbalancer"
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

def create_fake_zips(account_id, n_hours):
    db = DbHelper()
    lbs = db.get_lb_ids(account_id).values()
    now = datetime.datetime.now()
    for i in xrange(0,n_hours):
        dt = now + datetime.timedelta(hours=-i)
        for lb in lbs:
            (aid, lid, name) = lb
            utils.log("writing %s %s %s\n",
                       aid, lid, name)
            file_name = utils.set_local_file(aid, lid, dt)
            dir_name = os.path.split(file_name)[0]
            utils.mkdirs_p(dir_name)
            with zipfile.ZipFile(file_name, mode="w",
                            compression=zipfile.ZIP_DEFLATED) as zf:
                str_list = []
                for i in xrange(0,4096):
                    str_list.append("Line %i\n"%i)
                data = string.join(str_list,"")
                zf.writestr('test_log.txt', data)


def get_container_zips():
    db = DbHelper()
    czs = []
    lb_map = db.get_lb_map()
    zfiles = scan_zip_files(utils.incoming)
    for (aid, lid, hl, zip_file) in zfiles:
        if lid not in lb_map:
            utils.log("lid %i not found in database skipping\n", lid)
            continue
        (j1, j2, name) = lb_map[lid] #Throw away j1 and j2
        cnt = utils.get_container_name(lid, name, hl)
        zip = utils.get_remote_file_name(lid, name, hl)
        czs.append( (aid,lid, hl, zip_file, cnt, zip) )
        utils.sort_container_zips(czs)
    return czs


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


def parse_zip_name(zip_path):
    m = utils.zip_re.match(zip_path)
    if m:
        zip_file = m.group(1)
        aid = int(m.group(2))
        lid = int(m.group(3))
        hl = int(m.group(4))
        return (zip_file, aid, lid, hl, zip_path)
    return None