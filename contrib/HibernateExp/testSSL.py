#!/usr/bin/env jython

import util
util.setConfig("stag.json")
from util import *

s = SslTermTest(stubs,"./key","./crt","./chain")

s.setCF(api=True,chain=True)


s.setCF(api=False,chain=True)

s.addCert("test")

s.delCert("test")
