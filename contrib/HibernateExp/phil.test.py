#!/usr/bin/env jython
	
import util
util.setConfig("slice.json")
from util import *

stubs.ce.getFileNames()
String(stubs.ce.downloadFile("global_error.html"))


vnames = stubs.vs.getVirtualServerNames()
protocols = stubs.vs.getProtocol(vnames)

for i in xrange(0,len(vnames)):
    if protocols[i].toString() != "http":
       continue
    print vnames[]

httpvs = [v for (v,p) in zip(vnames,protocols) if p.toString()=="http"]
errorfiles = ["global_error.html"]*len(httpvs)

stubs.vs.setErrorFile(httpvs, errorfiles)
