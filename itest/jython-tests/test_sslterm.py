#!/usr/bin/env jython

from utils import *

SFU = StaticFileUtils
STH = SslTerminationHelper
st = SslTermination()
st.setPrivatekey(SFU.readFileToString("~/ssl.key"))
st.setReEncryptionCertificateAuthority(SFU.readFileToString("~/ca.crt"))
st.setIntermediateCertificate(SFU.readFileToString("~/imd.crt"))
st.setCertificate(SFU.readFileToString("~/ssl.crt"))

paths = STH.suggestCaPaths(st)
