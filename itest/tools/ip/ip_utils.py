
import netaddr
import sys
import os

def printf(format,*args): sys.stdout.write(format%args)

class IpMapper(object):
    def __init__(self,cidr_file="./cidrs.dat"):
        if cidr_file:
            self.load_cidrs_file(cidr_file)

    def load_cidrs_file(self, cidr_file):
        self.cidr_map = {}
        fp = open(cidr_file,"r")
        cidr_data = fp.read()
        fp.close()
        cidr_data = cidr_data.replace("\r","") #Get read of \r\n pairs
        for line in cidr_data.split("\n"):
            keypair = line.split("=")
            if len(keypair) <2:
                continue
            (key, value) = (keypair[0].strip(), keypair[1].strip())
            try:
                ipn = netaddr.IPNetwork(key)
                self.cidr_map[key] = (value, ipn)
            except:
                printf("Error trying to load network \"%s\"", key)
                raise

    def lookup(self, ip):
        matches = []
        for (cidr, (value, ipn)) in self.cidr_map.iteritems():
            if ip in ipn:
                matches.append(value)
        return matches



