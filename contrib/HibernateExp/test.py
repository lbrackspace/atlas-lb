#!/usr/bin/env jython
	
import com.zxtm.service.client.CertificateFiles as CertificateFiles
import util
util.setConfig("local.json")
from util import *


begin()
lb = qq("SELECT l FROM LoadBalancer l where l.id=38899")[0]
lb.getNodes()
commit()

begin()
c = qq("SELECT c FROM Cluster c where c.id=1")[0]
h = qq("SELECT h FROM Host h where h.id=1")[0]
lbs = newLoadBalancers(999999,20000,[h])
nodes = newNodes(lbs,ri(1,10))
saveList(nodes)
saveList(lbs)
commit()



#create a bunch of stuff
while True:
    begin()
    c = qq("SELECT c FROM Cluster c where c.id=1")[0]
    h = qq("SELECT h FROM Host h where h.id=1")[0]
    lbs = newLoadBalancers(aid,5,[h])
    n = ri(1,10)
    nodes = newNodes(lbs,n)
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

attempted HQL version of MySQLQuery:

select count(*) as nodes,loadbalancer_id from node 
     where loadbalancer_id in (select id from loadbalancer where account_id = 354934) 
         group by loadbalancer_id;



