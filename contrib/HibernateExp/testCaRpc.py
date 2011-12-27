#!/usr/bin/env python
import org.hexp.hibernateexp.util.SslNonsense.SecurityBorker as SecurityBorker
import xmlrpclib
import sys
import os

def printf(format,*args): sys.stdout.write(format%args)

SecurityBorker.bork() #Disables PKIX validation

cred={"user":"causr","passwd":"capasswd"}
url = "https://127.0.0.1:9876"

s = xmlrpclib.ServerProxy(url)

s.echo(cred,"I say hello")

