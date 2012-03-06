#!/usr/bin/env jython
	
import com.zxtm.service.client.CertificateFiles as CertificateFiles
import util
<<<<<<< HEAD
util.setConfig("ndev.json")
from util import *

begin()

q  = "SELECT lbe from LoadBalancerServiceEvent lbe"
q += "     WHERE lbe.author = 'dead2hill' ORDER BY lbe.created ASC"

startTime = isoTocal("2012-02-02T11:35:00")
endTimee  = isoTocal("2012-02-02T11:50:00")

res = qq(q)

bunkQ  = "SELECT lbe FROM LoadBalancerServiceEvent lbe WHERE lbe.author ="
bunkQ += ":author AND lbe.created BETWEEN :startDate AND :endDate"
bunkO += " ORDER BY lbe.created ASC"

=======
util.setConfig("ndev.json",skipDb=True)
from util import *

s = ZeusTest(stubs)

nodes = [["50.56.125.120:80","184.106.70.249:80"]]
pools = ["354934_211"]

tmp = s.stubs.p.getNodesPriorityValue(pools,nodes)
pris = [(p.getNode(),p.getPriority()) for p in tmp[0]]

penabled = s.stubs.p.getPriorityEnabled(pools)

pnodes = s.stubs.p.getPriorityNodes(pools)
>>>>>>> 1.10-candidate
