#!/usr/bin/env python

import hashlib
import string
import sys
import os

nummap = [c for c in "0123456789abcdefghijklmnopqrstuvwxyz"]

class IPv6(object):
    def __init__(self,ip):
        if type(ip) == type(""):
           self.ip = self.from_string(ip)
           return
        if type(ip) == type(0L):
            self.ip = ip
            return
        if type(ip) == type(0):
            self.ip = long(ip)
            return

    def to_string(self):
        ip_num = self.ip
        out = []
        for i in xrange(0,8):
            frag = ip_num&0xffff
            frag_string = itoa(frag,16)
            ip_num = ip_num >> 16
            out.append(pad(frag_string,4))
        out.reverse()
        return string.join(out,":")

    def from_string(self,ip_str):
        expanded_str = expand_str(ip_str,8)
        ip = 0
        for frag_str in expanded_str.split(":"):
            ip = ip << 16
            ip += int(frag_str,16)
        return ip

def sha1bits(account_id):
    sha1bytes = (hashlib.sha1("%s"%account_id).digest())[0:4]
    sha1bits = 0
    sha1bits |= ord(sha1bytes[0])<<24
    sha1bits |= ord(sha1bytes[1])<<16
    sha1bits |= ord(sha1bytes[2])<<8
    sha1bits |= ord(sha1bytes[3])<<0
    return sha1bits

def pad(val_str,digits):
    return "0"*(digits-len(val_str))+val_str

def itoa(num,base):
  out = ""
  if num == 0:
    return "0"
  if num < 0:
    return None
  if base <2 or base > len(nummap):
    return None
  while num > 0:
    digit = num % base
    out = nummap[digit] + out
    num /=base
  return out


#this is all you care about
def get_ipv6(cluster_bits, account_id, vip_octets):
    account_bits = sha1bits(account_id)
    #Vip octets are an integer so they are already "bits"
    ipv6_bits = (cluster_bits<<64) | (account_bits << 32) | vip_octets
    ipv6_str = IPv6(ipv6_bits).to_string()
    return ipv6_str

