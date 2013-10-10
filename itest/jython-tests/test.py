#!/usr/bin/env jython

from zxtm import *
from utils import *
from db import *

#getDb()
#zt = getZeusTest(hid=1)

vipsj = readFile("./vips.json")
vipsn = JPU.getNode(vipsj)

vipj = readFile("./vip.json")
vipn = JPU.getNode(vipj)

metadataj = readFile("./metadata.json")
metadatan = JPU.getNode(metadataj)

metaj = readFile("./meta.json")
metan = JPU.getNode(metaj)

nodesj = readFile("./nodes.json")
nodesn = JPU.getNode(nodesj)

nodej = readFile("./node.json")
noden = JPU.getNode(nodej)

vips = V1SF.newVirtualIps(3,3)

n = JPU.newObjectNode()



al = V1SF.newAccessList()	

n = JPU.newObjectNode()
JPS.attachAccessList(n,al,True)


n = JPU.getNode(n.toString())

al = JPD.decodeAccessList(n)



