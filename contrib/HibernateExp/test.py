#!/usr/bin/env jython
	
import com.zxtm.service.client.CertificateFiles as CertificateFiles
import util
util.setConfig("local.json")
from util import *

begin()
c = qq("SELECT c FROM Cluster c where c.id=1")[0]
h = qq("SELECT h FROM Host h where h.id=1")[0]

lbs = newLoadBalancers(354934,1,[h])
nodes = newNodes(lbs,10000)
saveList(nodes)
saveList(lbs)

lid = lbs[0].getId()
commit()

begin()
lb = qq("SELECT l from LoadBalancer l where l.id = %d"%lid)[0]

nodes = lb.getNodes()
