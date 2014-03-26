#!/usr/bin/env jython

from utils import *
import utils
from zxtm import *
from utils import *
from db import *

setConfFile("./dev.json")
app = getDb()
zt = getZeusTest(hid=1)

caPEM = readFile("./ca.crt")
keyPEM = readFile("./ssl.key")
sslPEM = readFile("./ssl.crt")

zt.stubs.ca.importCertificateAuthority(["354934_2008"],[caPEM])
zt.stubs.p.setSSLEncrypt(["354934_2008"],[True])
zt.stubs.p.setSSLStrictVerify(["354934_2008"],[True])

zt.stubs.vs.setSSLClientCertificateAuthorities(["354934_2008"],[["35493_2008"]])

SFU = StaticFileUtils
STH = SslTerminationHelper
st = SslTermination()
st.setPrivatekey(SFU.readFileToString("~/ssl.key"))
st.setReEncryptionCertificateAuthority(SFU.readFileToString("~/ca.crt"))
st.setIntermediateCertificate(SFU.readFileToString("~/imd.crt"))
st.setCertificate(SFU.readFileToString("~/ssl.crt"))

paths = STH.suggestCaPaths(st)




zt.stubs.ca.deleteCertificateAuthority(["354934_2008"])
