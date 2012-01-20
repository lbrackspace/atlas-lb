#!/usr/bin/env jython
import org.hexp.hibernateexp.util.SslNonsense.SecurityBorker as SecurityBorker
import xmlrpclib
import random
import copy
import sys
import os

def printf(format,*args): sys.stdout.write(format%args)

def rndName(n):
    name = ""
    for i in xrange(0,n):
        name += rnd.choice(letters)
    return name

def genCrt(name):
    

letters = "abcdefghijklmnopqrstuvwxyz"

rnd = random.Random()

SecurityBorker.bork() #Disables PKIX validation

cred={"user":"causr","passwd":"capasswd"}
url = "https://127.0.0.1:9876"

s = xmlrpclib.ServerProxy(url)

base_subj = {}
base_subj["C"]="US"
base_subj["ST"]="Texas"
base_subj["L"]="San Antonio"
base_subj["O"]="RackExp"

name = rndName(8)

subj = copy.deepcopy(base_subj)
subj["CN"] = "%s.%s"%(name,"rackexp.org")
subj["OU"] = "%s unit"%(name)




r = s.genCrt(cred,2048,subj)

(key,crt) = (r["key",r["crt"])
