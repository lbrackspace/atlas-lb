import unittest
from time import sleep

import ConfigParser

import client.api as apic
import client.stm as stmc


class ConcurrencyTest(unittest.TestCase):

    def test(self):
        filename = 'config.cfg'
        config = ConfigParser.SafeConfigParser()
        config.read([filename])

        aurl = config.get('main', 'aurl')
        acctid = config.get('main', 'acctid')
        tok = config.get('main', 'token')
        lid = config.get('main', 'lbid')
        user = config.get('main', 'user')
        passwd = config.get('main', 'passwd')

        aclient = apic.ApiClient(aurl, tok)
        cnodes = aclient.get_nodes(lid)
        if not cnodes:
            aclient.create_nodes(100, lid)
            cnodes = aclient.get_nodes(lid)
        aclient.multi_del_node(lid, 20, cnodes)
        aclient.wait_for_active(lid)

        nodes = aclient.get_nodes(lid)
        self.assertEqual(0, len(nodes))

        #sync lag in stm
        sleep(3)
        surl = config.get('main', 'stmurl')
        vs_id = '%s_%s' % (acctid, lid)
        #stmcli = stmc.StmClient(surl, user, passwd)
        #self.assertEqual(0, len(stmcli.get_pool_nodes(vs_id)))

    def test2(self):
        filename = 'config.cfg'
        config = ConfigParser.SafeConfigParser()
        config.read([filename])

        aurl = config.get('main', 'aurl')
        acctid = config.get('main', 'acctid')
        tok = config.get('main', 'token')
        lid = config.get('main', 'lbid')
        user = config.get('main', 'user')
        passwd = config.get('main', 'passwd')

        aclient = apic.ApiClient(aurl, tok)
        lb = aclient.create_lb(None, 9090, True)
        self.assertIsNotNone(lb)
        vips = lb['loadBalancer']['virtualIps']
        vip_id = None
        for v in vips:
            if v['ipVersion'] == "IPV4":
                vip_id = v['id']

        aclient.batch_create_shared_lb(5, vip_id, False)


