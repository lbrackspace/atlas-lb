from multiprocessing import Pool
from random import randrange
import requests
import json
import traceback


class ApiClient():
    def __init__(self, api_url, token):
        self.result_list = []
        self.api_url = api_url
        self.token = token

    def get_headers(self):
        #Read from file
        return {'X-Auth-Token': self.token,
                'content-type': 'application/json'}

    def wait_for_active(self, lb_id):
        url = '%s/%s' % (self.api_url, lb_id)
        status = None
        while not status == "ACTIVE":
            print "Polling LB %s status ..." % lb_id
            r = requests.get(url, headers=self.get_headers())
            data = json.loads(r.content)
            status = data['loadBalancer']['status'].encode('utf-8')
            print "LB %s is %s..." % (lb_id, status)
            if status == 'ERROR':
                print "LB %s is ERROR" % lb_id
                return status
        return status

    def create_nodes(self, ncount, lb_id):
        url = '%s/%s/nodes' % (self.api_url, lb_id)
        nodes = {"nodes": []}

        for i in range(ncount):
            node = {"address": self.gen_ip(),
                    "port": 80,
                    "condition": 'ENABLED',
                    "type": 'PRIMARY'}
            nodes['nodes'].append(node)

        r = requests.post(url, data=json.dumps(nodes),
                          headers=self.get_headers())
        data = json.loads(r.content)
        print json.dumps(data, indent=2)
        status = self.wait_for_active(lb_id)
        if status is "ERROR":
            print "LB is in ERROR status!"
            return None
        return data

    def create_lb(self, v, port, wait):
        url = '%s/' % self.api_url
        if port is None:
            port = 80
        if v is None:
            v = "PUBLIC"
            type="type"
        else:
            type="id"
        lb = {"loadBalancer": {
            "name": "a-new-loadbalancer",
            "port": port,
            "protocol": "HTTP",
            "virtualIps": [
                {
                    type: v
                }
            ],
            "nodes": [
                {
                    "address": "10.1.1.1",
                    "port": 80,
                    "condition": "ENABLED"
                }
            ]
        }
        }

        r = requests.post(url, data=json.dumps(lb),
                          headers=self.get_headers())
        data = json.loads(r.content)
        print json.dumps(data, indent=2)
        if wait is not False:
            status = self.wait_for_active(data["loadBalancer"]["id"])
            if status is "ERROR":
                print "LB is in ERROR status!"
                return None
        return data

    def batch_create_shared_lb(self, lbcount, vip_id, wait):
        pool = Pool(processes=lbcount)
        for _ in range(lbcount):
            pool.apply_async(self.create_lb(vip_id, None, wait), args=(self, ),
                             callback=self.log_result)
        pool.close()
        pool.join()
        print(self.result_list)


    def gen_ip(self):
        not_valid = [10, 127, 169, 172, 192]

        first = randrange(1, 256)
        while first in not_valid:
            first = randrange(1, 256)

        ip = ".".join([str(first), str(randrange(1, 256)),
                       str(randrange(1, 256)), str(randrange(1, 256))])
        print ip
        return ip

    def get_nodes(self, lb_id):
        url = '%s/%s/nodes' % (self.api_url, lb_id)
        r = requests.get(url, headers=self.get_headers())
        data = json.loads(r.content)
        return data['nodes']

    def del_nodes(self, lb_id, n_id):
        url = "%s/%s/nodes/%s" % (self.api_url, lb_id, n_id)
        status = None
        while True:
            print 'Attempting to remove node %s' % n_id
            try:
                r = requests.delete(url, headers=self.get_headers())
                status = r.status_code
                if status != 202:
                    data = json.loads(r.content)
                    if status == 404 or status == 500:
                        print json.dumps(data, indent=2)
                        break
                    elif status == 422:
                        r = json.dumps(data, indent=2)
                        if "ERROR" in r:
                            print "LB is in ERROR status..."
                            break
                    else:
                        print json.dumps(data, indent=2)

                else:
                    print 'Status %s for node %s' % (status, n_id)
                    break
            except:
                print "Delete node failed: ", traceback.format_exc()


    def multi_del_node(self, lb_id, tcount, nodes):
        pool = Pool(processes=tcount)
        for n in nodes:
            pool.apply_async(self.del_nodes(lb_id, n['id']), args=(self, ),
                             callback=self.log_result)
        pool.close()
        pool.join()
        print(self.result_list)

    def log_result(self, result):
        self.result_list.append(result)


        #def go(self):
        #    cnodes = self.get_nodes()
        #    if not cnodes:
        #        self.create_nodes()
        #        cnodes = self.get_nodes()
        #    self.run(cnodes)
        #    #again..
        #    self.wait_for_active()