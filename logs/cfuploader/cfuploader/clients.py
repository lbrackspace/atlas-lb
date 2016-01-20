
import MySQLdb.cursors
import swiftclient
import datetime
import requests
import MySQLdb
import json
import os


class Config(object):
    def __init__(self, conf_file="/etc/cfuploader/cfuploader.json"):
        conf = self.load_json(conf_file)
        self.auth_url = conf['auth_url']
        self.auth_user = conf['auth_user']
        if self.auth_url[-1] == "/":
            self.auth_url = self.auth_url[:-1]
        self.auth_passwd = conf['auth_passwd']
        self.db = conf['db']
        self.conf = conf

    def load_json(self, pathIn):
        return json.loads(open(os.path.expanduser(pathIn),"r").read())

    def save_json(self, pathOut, obj):
        open(os.path.expanduser(pathOut),"w").write(json.dumps(obj,indent=2))

account_lb_query = """
select account_id, id, name from loadbalancer
where account_id = %s and status = 'ACTIVE'
"""

class Auth(object):
    def __init__(self, conf=None):
        if conf == None:
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
        return json.loads(r.text)

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
