#!/usr/bin/env python

import traceback
import string
import copy
import re

addr_re_str  = r"^([0-9]{1,3})\." # 1st octet
addr_re_str += r"([0-9]{1,3})\." # 2nd octet
addr_re_str += r"([0-9]{1,3})\." # 3rd second octet
addr_re_str += r"([0-9]{1,3})$" # 4th octet
ipblock_re = re.compile("(.*)/(.*)",re.I)

addr_re = re.compile(addr_re_str)
class NetCidr(object):
    def wildcard(self,cidr_in):
        mask_out = ~self.mask_int(cidr_in)&0xffffffff
        return mask_out

    def range_mask(self,ip1,ip2):
        cidr = self.range_cidr(ip1,ip2)
        return self.mask_int(cidr)

    def range_cidr(self,ip1,ip2):
        ip_diff = ip1^ip2
        mask_out = 0
        msb = 0
        while ip_diff >0:
            msb += 1
            ip_diff >>= 1
        return 32-msb

    def host_range(self,ip,mask):
        hi = self.highest_host(ip,mask)
        lo = self.lowest_host(ip,mask)
        return (lo,hi)

    def xrangeipstr(self,lo,hi):
        out = []
        ip = lo
        while ip <= hi:
            yield self.iptostr(ip)
            ip += 1

    def lowest_host(self,ip,mask):
        return (ip&mask) + 1 #do not return subnet id

    def highest_host(self,ip,mask):
        cidr = self.cidr_int(mask)
        return (ip|self.wildcard(cidr))-1 # do not return broad case address

    def cidr_int(self,mask_in):
        if mask_in == 4294967295:
            return 32
        rolling_bit = 1<<32
        bit_i = 32
        cidr_count = 0
        while True:
            bit_i -= 1
            if (1<<bit_i)&mask_in > 0:
                cidr_count += 1
                continue
            else:
                break
        return cidr_count

    def net_id(self,ip_in,cidr_in):
        return ip_in & self.mask_int(cidr_in)

    def mask_int(self,cidr_in):
        if cidr_in < 0 or cidr_in >= 1<<32:
            return None
        mask_out = 0
        rolling_bit = 1<<32
        for i in xrange(0,cidr_in):
            rolling_bit >>= 1
            mask_out |= rolling_bit
        return mask_out

    def octtoip(self,octets_in):
        ip_out = 0
        octet = copy.copy(octets_in)
        octet.reverse()
        for i in xrange(0,len(octet)):
            ip_out += octet[i] << i*8
        return ip_out

    def iptooct(self,ip):
        octets = []
        for i in xrange(0,4):
            octets.append("%s"%((ip>>i*8)&255))
        octets.reverse()
        return octets

    def iptostr(self,ip):
        octets = self.iptooct(ip)
        out_str = string.join(octets,".")
        return out_str

    def strtoip(self,addr_string):
        oct = self.strtooct(addr_string)
        ip = self.octtoip(oct)
        return(ip)

    def strtooct(self,addr_string):
        m = addr_re.match(addr_string)
        if m:
            octets = []
            for i in xrange(1,5):
                x = int(m.group(i))
                if x<0 or x>255:
                    return None
                octets.append(x)
            return octets
        raise "INVALID IP STRING"
        return None

    def ipstr_isvalid(self,ip_str):
        try:
            oct = self.strtooct(ip_str)
            if oct == None:
                return False
        except:
            return False
        return True


    def strtoipblock(self,block_str):
        m = ipblock_re.match(block_str)
        if not m:
            return (None,None)
        ip = self.strtoip(m.group(1))
        mask = self.mask_int(int(m.group(2)))
        return (ip,mask)

    def ipset(self,net_id,mask,bc=False,ni=False):
        out = set()
        (lo,hi) = self.host_range(net_id,mask)
        if ni:
            lo -= 1
        if bc:
            hi += 1
        if (hi-lo) > 67108864: #Anything greater then 26 bits
            return None #Error operation would consume to much memory
        ip = lo
        while ip <= hi:
            out.add(ip)
            ip += 1
        return out

    def ipstrs2ipset(self,ipstrs):
        out = set()
        for ip_str in ipstrs:
            out.add(self.strtoip(ip_str))
        return out

    def block_isvalid(self,block_str):
        m = ipblock_re.match(block_str)
        if not m:
            return False
        try:
            ip_str = m.group(1)
            mask_int = int(m.group(2))
            if mask_int <0 or mask_int >= 1<<32:
                return False
            oct = self.strtooct(ip_str)
            for o in oct:
                if o == None:
                    return False
        except:
            print traceback.format_exc()
            return False
        return True
