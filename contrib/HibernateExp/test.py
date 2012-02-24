#!/usr/bin/env jython
	
import com.zxtm.service.client.CertificateFiles as CertificateFiles
import util
util.setConfig("slice.json")
from util import *

begin()
nds = qq("SELECT ad from AllowedDomains ad")



ro = AllowedDomains()
ro.setName("rackexp.org")
vn = AllowedDomains()
vn.setName("viralnotes.com")
tn = AllowedDomains()
tn.setName("thesenotions.com")

saveList([ro,vn,tn])
