#!/usr/bin/env jython
	
import com.zxtm.service.client.CertificateFiles as CertificateFiles
import util
util.setConfig("ndev.json")
from util import *


begin()

qStr  = "SELECT lb.healthMonitor.id from LoadBalancer lb "
qStr += "    where lb.accountId=354932 and lb.id=221"

qStr  = "SELECT lb.id from LoadBalancer lb "
qStr += "    where lb.accountId=354934 and lb.id = 221"

