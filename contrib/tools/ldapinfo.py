#!/usr/bin/env python

import getpass
import urllib2
import base64
import string
import json
import sys
import os

def printf(format,*args): sys.stdout.write(format%args)

def fprintf(fp,format,*args): fp.write(format%args)

def chop(strIn):
    return strIn.replace("\n","").replace("\r","")

def getuserpass(uprompt="Enter user name: ",pprompt="passwd: ",fp=sys.stdout):
    fprintf(fp,"%s",uprompt)
    user   = chop(sys.stdin.readline())
    passwd = getpass.getpass(pprompt)
    return (user,passwd)

def basicAuthHeader(user,passwd):
    args = (user.encode("ascii","ignore"),passwd.encode("ascii","ignore"))
    b64 = base64.b64encode("%s:%s"%args)
    return {"Authorization":"BASIC %s"%b64}

def stripUrl(urlIn):
    return string.join(urlIn.split("/")[:3],"/")
    

def ldapInfoReq(baseUrl,user,passwd):
    url = stripUrl(baseUrl)
    uri = "/v1.0/management/stub/ldapinfo"
    headers = {}
    headers["content-type"]="application/json"
    headers["accept"]="application/json"
    headers.update(basicAuthHeader(user,passwd))
    req = urllib2.Request(url + uri,headers=headers)
    return req

def ldapInfoResp(req):
    resp = urllib2.urlopen(req)
    jsonStr = resp.read()
    obj = json.loads(jsonStr)
    jsonStr = json.dumps(obj,indent=4)
    return jsonStr
    
if __name__ == "__main__":
    (user,passwd) = getuserpass()
    printf("Enter management url: ")
    sys.stdout.flush()
    baseUrl = chop(sys.stdin.readline())
    req = ldapInfoReq(baseUrl,user,passwd)
    try:
        jsonStr = ldapInfoResp(req)
    except urllib2.HTTPError, e:
        printf("error:\ncode=%s\nex=%s\n",e.code,e.read())
        sys.exit()
    printf("%s\n",jsonStr)
    
