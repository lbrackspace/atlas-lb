import threading
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

cfg = utils.cfg

printf = utils.printf

class Auth(object):
    def __init__(self, conf=None):
        if conf is None:
            conf = utils.Config()
        self.conf = conf
        self.auth_url = conf.auth_url
        self.auth_user = conf.auth_user
        self.auth_passwd = conf.auth_passwd
        self.req_count = 0
        self.cache_count = 0
        self.auth_lock = threading.Lock()
        #Cache objects
        self.god_token = None
        self.god_expires = None
        self.all_users = {}
        self.admin_users = {}
        self.user_tokens = {}
        self.endpoints = {}
        self.token_and_endpoints = {}
        #Cache counters
        self.req_count = 0
        self.cache_count = 0


    def prep_headers(self):
        with self.auth_lock:
            return {'content-type': 'application/json',
                    'accept': 'application/json',
                    'x-auth-token': self.god_token}


    def get_endpoints_by_token(self, token):
        with self.auth_lock:
            if token in self.endpoints:
                self.cache_count += 1
                return self.endpoints[token]

        uri = self.auth_url + "/v2.0/tokens/{0}/endpoints".format(
            token)
        r = requests.get(uri, headers=self.prep_headers())
        resp = json.loads(r.text)
        with self.auth_lock:
            self.req_count += 1
            self.endpoints[token] = resp
        return resp


    def get_admin_by_user(self, uid):
        with self.auth_lock:
            if uid in self.admin_users:
                self.cache_count += 1
                return self.admin_users[uid]
        hdr = self.prep_headers()
        uri = self.auth_url + "/v2.0/users/{0}/RAX-AUTH/admins".format(uid)
        r = requests.get(uri, headers=hdr)
        obj = json.loads(r.text)
        try:
            #Verify we have 1 user with a name and id and a region
            user_id = obj['users'][0]['id']
            user_name = obj['users'][0]['username']
            region = obj['users'][0]['RAX-AUTH:defaultRegion']
        except:
            f = "ERROR getting admin user for uid %s %s\n"
            utils.log(f, uid, r.text)
            raise
        with self.auth_lock:
            self.req_count += 1
            self.all_users[uid] = obj
            return obj

    def get_admin_token(self):
        with self.auth_lock:
            if self.god_token:
                self.cache_count += 1
                return {"token": self.god_token, "expires":self.god_expires}
               
        up = {'username': self.auth_user, 'password': self.auth_passwd}
        payload = {'auth': {'passwordCredentials': up}}
        hdr = self.prep_headers()
        uri = self.auth_url + "/v2.0/tokens"
        r = requests.post(uri, headers=hdr, data=json.dumps(payload))
        obj = json.loads(r.text)
        token = obj["access"]["token"]["id"]
        expires = obj["access"]["token"]["expires"]
        with self.auth_lock:
            self.god_token = token
            self.god_expires = expires
            self.req_count += 1        
            return {"token": token, "expires": expires}

    def get_all_users(self, domain_id):
        with self.auth_lock:
            if domain_id in self.all_users:
                self.cache_count += 1
                return self.all_users[domain_id]
            
        uri = self.auth_url + "/v2.0/RAX-AUTH/domains/{0}/users".format(
            domain_id)
        r = requests.get(uri, headers=self.prep_headers())
        try:
            obj = json.loads(r.text)
            user = obj['users'][0]['id'] #Does this have at least 1 user
            with self.auth_lock:
                self.req_count += 1
                self.all_users[domain_id] = obj
            return obj
        except:
            f = "ERROR getting no users found for aid %d %s\n"
            utils.log(f, domain_id, r.text)
            raise 

    #Not used but leave it for debugging
    def get_endpoints(self, domain_id):
        uri = self.auth_url + "/v2.0/RAX-AUTH/domains/{0}/endpoints".format(
                              domain_id)
        hdr = self.prep_headers()
        r = requests.get(uri, headers=hdr)
        return json.loads(r.text)

    #not used but leave it for debugging
    def get_username_and_region(self, user):
        try:
            username = user['username']
            region = user['RAX-AUTH:defaultRegion']
        except:
            f= """ERROR looking up user %i resp was "%s" headers were "%s"\n"""
            utils.log(f, domain_id, r.text, r.headers)
            printf(f, domain_id, r.text, r.headers)
            raise
        return {'user': username, 'region': region}

    def impersonate_user(self, username):
        with self.auth_lock:
            if username in self.user_tokens:
                self.cache_count += 1
                return self.user_tokens[username]
        uri = self.auth_url + "/v2.0/RAX-AUTH/impersonation-tokens"
        imp = {'user': {'username': username}, 'expire-in-seconds': 3600}
        obj = {'RAX-AUTH:impersonation': imp}
        json_data = json.dumps(obj)
        r = requests.post(uri, headers=self.prep_headers(), data=json_data)
        obj = json.loads(r.text)
        try:
            token_id = obj['access']['token']['id']
        except:
            f= """ERROR impersonating %s resp was "%s" headers were "%s"\n"""
            utils.log(f, username, r.text, r.headers)
            printf(f, username, r.text, r.headers)
            raise
        with self.auth_lock:
            self.req_count += 1
            self.user_tokens[username] = token_id
            return token_id

    def get_token_and_endpoint(self, domain_id):
        with self.auth_lock:
            if domain_id in self.token_and_endpoints:
                self.cache_count += 1
                return self.token_and_endpoints[domain_id]
        #Grab all users for this ddi
        domain_users  = self.get_all_users(domain_id)
        #Figure out who the admin is by looking up the first users admin
        first_user_id = domain_users['users'][0]['id']
        admin_users = self.get_admin_by_user(first_user_id)
        admin_user_name = admin_users['users'][0]['username']
        admin_region = admin_users['users'][0]['RAX-AUTH:defaultRegion']
        token = self.impersonate_user(admin_user_name)
        eps = self.get_endpoints_by_token(token)
        cf_eps = [e for e in eps['endpoints'] if e['type'] == 'object-store']
        cf_endpoint = None
        for ep in cf_eps:
            if ep['region'] == admin_region:
                cf_endpoint = ep['publicURL']
                break

        out = {'token': token, 'cf_endpoint': cf_endpoint}
        if cf_endpoint is None:
            msg = ''.join([
                "Error coulden't get endpoint for aid={0} pu={1} " +
                "token={2} eps={3}\n"]).format(domain_id, admin_user_name,
                                               token, eps)
            raise Exception(msg)
        with self.auth_lock:
            self.cache_count += 1
            self.token_and_endpoints[domain_id] = out
            return out

    def get_counts(self):
        with self.auth_lock:
            return {"cache": self.cache_count, "reqs": self.req_count,
                    "total": self.cache_count + self.req_count}

    def clear_cache(self):
        with self.auth_lock:
            self.god_token = None
            self.god_expires = None
            self.all_users = {}
            self.user_tokens = {}
            self.user_tokens = {}
            self.endpoints = {}
            self.token_and_endpoints = {}



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

    def list_container(self, name):
        resp = self.con.get_container(name, full_listing=True)
        return resp

    def delete_object(self, cnt, obj):
        resp = self.con.delete_object(cnt, obj)
        return resp

    #Only for cleaning up testing directories. Code should not be used
    #in production script
    def empty_container(self, cnt):
        (info, objs) = self.con.get_container(cnt, full_listing=True)
        for obj in objs:
            self.con.delete_object(cnt, obj['name'])
            yield (cnt, obj['name'])

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
        if conf is None:
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
            name = row['name'].replace(" ", "_").replace("/", "_")
            rows_out[lid] = (aid, lid, name)
        return rows_out

    def get_lb_ids(self, aid_str):
        aid = int(aid_str)
        con = MySQLdb.connect(**self.db)
        cur = con.cursor(MySQLdb.cursors.DictCursor)
        cur.execute(account_lb_query, (aid,))
        rows = cur.fetchall()
        cur.close()
        con.close()
        rows_out = {}
        for row in rows:
            aid = row['account_id']
            lid = row['id']
            name = row['name'].replace(" ", "_").replace("/", "_")
            rows_out[lid] = (aid, lid, name)
        return rows_out


def create_fake_zips(account_id, n_hours):
    db = DbHelper()
    lbs = db.get_lb_ids(account_id).values()
    now = datetime.datetime.now()
    for i in xrange(0, n_hours):
        dt = now + datetime.timedelta(hours=-i)
        for lb in lbs:
            (aid, lid, name) = lb
            utils.log("writing %s %s %s\n",
                      aid, lid, name)
            full_path = utils.set_local_file(aid, lid, dt)
            dir_name = os.path.dirname(full_path)
            utils.mkdirs_p(dir_name)
            with zipfile.ZipFile(full_path, mode="w",
                                 compression=zipfile.ZIP_DEFLATED) as zf:
                str_list = []
                for j in xrange(0, 4096):
                    str_list.append("Line %i\n" % j)
                data = string.join(str_list, "")
                zf.writestr('test_log.txt', data)


def get_container_zips():
    db = DbHelper()
    czs = []
    lb_map = db.get_lb_map()
    zfiles = scan_zip_files(utils.cfg.incoming)
    for zf in zfiles:
        if zf['lid'] not in lb_map:
            utils.log("lid %i not found in database skipping\n", zf['lid'])
            continue
        (zf['aid'], j2, zf['name']) = lb_map[zf['lid']]
        zf['cnt'] = utils.get_container_name(zf['lid'], zf['name'], zf['hl'])
        zf['remote_zf'] = utils.get_remote_file_name(zf['lid'],
                                                     zf['name'], zf['hl'])
        czs.append(zf)
    utils.sort_container_zips(czs)
    return czs


def scan_zip_files(file_path):
    zip_files = []
    for (root, dirnames, file_names) in os.walk(file_path):
        for file_name in file_names:
            full_path = os.path.join(root, file_name)
            pzn = utils.parse_zip_name(full_path)
            if pzn:
                zip_files.append(pzn)
    return zip_files




