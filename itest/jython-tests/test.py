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

vips = V1SF.newVirtualIps(3,3)

n = JPU.newObjectNode()
