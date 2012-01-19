#!/usr/bin/env jython

import util
util.setConfig("local.json")
from util import *

s = SslTermTest(stubs,"./key","./crt","./chain")

#s.getCrtNames()
#s.getVsNames()

s.setCrtName("TEST")
s.setVsName("354934_41")

s.setCF(api=True,chain=True)

s.setCF(api=False,chain=True)

s.addCrt()
s.setVsCrt()
s.sslOn()


s.sslOff()
s.delCrt()


