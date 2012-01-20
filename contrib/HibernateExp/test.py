#!/usr/bin/env jython
	
import com.zxtm.service.client.CertificateFiles as CertificateFiles
import util
util.setConfig("local.json")
from util import *

certNames = stubs.cert.getCertificateNames()
certs = {}
certsRaw = {}

#stubs.cert.deleteCertificate(["test"])

i = 0
for certInfo in stubs.cert.getCertificateInfo(certNames):
    certs[certNames[i]] = certInfo
    i += 1

i = 0
for rawCert in stubs.cert.getRawCertificate(certNames):
    certsRaw[certNames[i]] = rawCert
    i += 1




#create new cert and Key and save  through zeus
key_file = "smurf.key"
crt_file = "smurf.crt"
name = "smurfette.smurfvilliage.com"
key = open(key_file,"r").read()
crt = open(crt_file,"r").read()

#not sure why zeus named this class plural since its singular
cf = CertificateFiles()# In java its CertificateFiles cf = new CertificateFiles()
cf.setPrivate_key(key)
cf.setPublic_cert(crt)

#save it in zeus
stubs.cert.importCertificate([name],[cf])
#Don't forget stubs.cert is an initialized CatalogSSLCertificatesBindingStub obj



baseSubj = "C=US,ST=Texas,L=Texas,O=RackSpace Hosting"
subjs = []
chainPems = []
for i in xrange(1,10):
    subjs.append("%s,OU=RackExp CA%i,CN=ca%i.rackexp.org"%(baseSubj,i,i))




