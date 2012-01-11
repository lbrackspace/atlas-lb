#!/usr/bin/env jython

import org.hexp.hibernateexp.util.SslNonsense.SecurityBorker as SecurityBorker
import util
util.setConfig("stag.json",skipDb=True)
from util import *

import xmlrpclib
import re
import sys
import os

ssl_re = re.compile("^ssl[0-9]+$")

baseSubj = "C=US,ST=Texas,L=San Antonio,O=RackExp,OU=client TestSites"

SecurityBorker.bork()	

def usage(prog):
    printf("usage is %s <chain.json> <dnsxmlrpcconf.json> <baseDomain> ",prog)
    printf("<Shared ip> <vs_name> <number> <info.db>\n")
    printf("\n")

def getCFS(info):
    crtNames = []
    cfs = []
    certs = info["certs"]
    vs_name = info["vs_name"]
    ip = info["ip"]
    vss = []
    for (host,cb) in certs.items():
        cf = CertificateFiles(cb["crt"] + cb["chain"],cb["key"])
        crtName = "%s_%i"%(vs_name,cb["id"])
        crtNames.append(crtName)
        cfs.append(cf)
        vss.append(VirtualServerSSLSite(ip,crtName))
    return (crtNames,cfs,vss)

if __name__ == "__main__":
    prog = os.path.basename(sys.argv[0])
    if len(sys.argv)<8:
        usage(prog)
        sys.exit()
    chain = load_json(sys.argv[1])
    dnsconf = load_json(sys.argv[2])
    ip = sys.argv[4]
    basedomain = sys.argv[3]
    vs_name = sys.argv[5]
    ni = int(sys.argv[6])
    info_file = sys.argv[7]
    
    keyPem = String(chain[0]["key"]).getBytes("US-ASCII")
    caKey = RsaPair(PemUtils.fromPem(keyPem))

    caPem = String(chain[0]["crt"]).getBytes("US-ASCII")
    caCrt = PemUtils.fromPem(caPem)

    chainStr = ""
    for entry in chain:
        chainStr += entry["crt"]

    baseConf = dnsconf["domains"][basedomain]
    baseHost = baseConf["baseDomain"]
    cred     = baseConf["cred"]
    url      = baseConf["url"]    

    s = xmlrpclib.ServerProxy(url)

    z = ZeusTest(stubs)

    
    ip_vs = z.getInfo()["ip_vs"]
    vs_ip = z.getInfo()["vs_ip"]

    if ip not in set(ip_vs.keys()):
        printf("ip %s not found in vs %s exiting\n",ip,vs_name)
        sys.exit()


    certs = {}
    info = {}
    info["zxtm"] = z.getInfo()
    info["certs"] = certs
    info["vs_name"] = vs_name
    info["ip"] = ip
    for i in xrange(1,ni+1):
        host = "ssl%i"%i
        subj = "%s,CN=%s%s"%(baseSubj,host,baseHost)
        printf("Creating csr for subj \"%s\"\n",subj)
        clientKey = RSAKeyUtils.genRSAPair(2048,32)
        csr = CsrUtils.newCsr(subj,clientKey,False)
        crt = CertUtils.signCSR(csr,caKey,caCrt,730,None)
        cb = {}
        cb["id"] = i
        cb["key"] = "%s"%String(toPem(clientKey),"US-ASCII")
        cb["csr"] = "%s"%String(toPem(csr),"US-ASCII")
        cb["crt"] = "%s"%String(toPem(crt),"US-ASCII")
        cb["chain"]= chainStr
        cb["host"]="%s%s"
        certs[host] = cb
    save_cpickle(info_file,info)
    (certNames,cfs,vss) = getCFS(info)

