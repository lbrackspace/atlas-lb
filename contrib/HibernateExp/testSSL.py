#!/usr/bin/env jython

import util
util.setConfig("stag.json")
from util import *

s = SslTermTest(stubs,"./key","./crt","./chain")

#s.getCrtNames()
#s.getVsNames()

#ips = invips(getIPAddresses(stubs))
#working external vip is 184.106.24.18


s.setCrtName("TEST")
s.setVsName("546428_4")

s.setCF(api=True,chain=True)

s.setCF(api=False,chain=True)

s.addCrt()
s.setVsCrt()
s.sslOn()


s.sslOff()
s.delCrt()


