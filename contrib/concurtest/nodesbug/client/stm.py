from multiprocessing import Pool

import urllib
import requests
from requests.auth import HTTPBasicAuth

import json
import traceback


class StmClient():
    def __init__(self, stm_url, user, token):
        self.result_list = []
        self.stm_url = stm_url
        self.user = user
        self.token = token

    def get_headers(self):
        #Read from file
        return {'content-type': 'application/json'}


    def get_pool(self, vs_id):
        p = "http://phillip.toohill:pass@10.5.71.150:8051/"
        pd = {"https": p}
        r = requests.get('%s/pools/%s' % (self.stm_url, vs_id),
                         headers=self.get_headers(),
                         auth=HTTPBasicAuth(self.user, self.token),
                         verify=False)
        data = json.loads(r.content)
        return data['properties']

    def get_vs(self, vs_id):
        p = "http://phillip.toohill:pass@10.5.71.150:8051/"
        pd = {"https": p}
        r = requests.get('%s/virtual_servers/%s' % (self.stm_url, vs_id),
                         headers=self.get_headers(),
                         auth=HTTPBasicAuth(self.user, self.token),
                         verify=False)
                         #,proxies=pd)
        data = json.loads(r.content)
        return data

    def get_pool_nodes(self, vs_id):
        return self.get_pool(vs_id)['basic']['nodes']