#!/usr/bin/env jython
	
import com.zxtm.service.client.CertificateFiles as CertificateFiles
import util
util.setConfig("slice.json")
from util import *

begin()
c = qq("SELECT c FROM Cluster c where c.id=1")[0]
h = qq("SELECT h FROM Host h where h.id=1")[0]

lbs = newLoadBalancers(354934,1,[h])
nodes = newNodes(lbs,10)
saveList(nodes)
saveList(lbs)

commit()

begin()
txin(lbs)
txin(nodes)



begin()
lb = qq("SELECT l from LoadBalancer l where id=790")[0]
nodes = qq("SELECT n from Node n where n.loadbalancer.id=790")


oldPri = ZNPC(nodes)

newPri = ZNPC(nodes)


ZNPC.getAction(oldPri,newPri)

poolName = "%s_%s"%(lb.getAccountId(),lb.getId())

stubs.p.setNodesPriorityValue(["354934_790"],newPri.getPriorityValues())



stubs.p.setPriorityEnabled([poolName],[True])
