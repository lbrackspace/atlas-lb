#!/usr/bin/env jython

import util
util.setConfig("local.json")
from util import *

s = SslTermTest(stubs,"./key","./crt","./chain")
s.getNames()



s.setCF(api=True,chain=True)

s.setCF(api=False,chain=True)

s.showCF()

s.addCert("test")

s.delCert("test")
