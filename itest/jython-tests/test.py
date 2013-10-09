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

vips = JPD.decodeVirtualIps(JPU.getNode(vipsj))
vip = vips.getVirtualIps()[0]

obj = JPS.encodeVirtualIp(vips.getVirtualIps()[0],None)

jgw = JPU.newJsonGeneratorStringWriter()
jg = jgw.getJsonGenerator()
wr = jgw.getWriter()

jg.writeTree(obj)
