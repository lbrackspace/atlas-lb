#!/usr/bin/env jython
	
import com.zxtm.service.client.CertificateFiles as CertificateFiles
import util
util.setConfig("ndev.json",skipDb=True)
from util import *

s = ZeusTest(stubs)

nodes = [["50.56.125.120:80","184.106.70.249:80"]]
pools = ["354934_211"]

tmp = s.stubs.p.getNodesPriorityValue(pools,nodes)
pris = [(p.getNode(),p.getPriority()) for p in tmp[0]]

penabled = s.stubs.p.getPriorityEnabled(pools)

pnodes = s.stubs.p.getPriorityNodes(pools)
