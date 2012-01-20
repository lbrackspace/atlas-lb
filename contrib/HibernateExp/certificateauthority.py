import M2Crypto
import random
import copy
import time
import json
import sys
import os

rnd = random.Random()

nameMap = [("C","Country"),("ST","State or Province"),
    ("L","Locality"),("O","Organization"),("OU","Organizational Unit"),
    ("CN","Common Name"),("emailAddress","Email Address")]


def nop():
    pass

def fullPath(file_path):
    return os.path.abspath(os.path.expanduser(file_path))

def read_file(file_path):
    full_path = fullPath(file_path)
    fp = open(full_path,"r")
    data = fp.read()
    fp.close()
    return data

def write_file(file_path,data):
    open(fullPath(file_path),"w").write(data)

def save_file(file_path,data):
    open(fullPath(file_path),"w").write(data)

def load_json(json_file):
    full_path = os.path.expanduser(json_file)
    full_path = os.path.abspath(full_path)
    fp = open(full_path,"r")
    json_data = fp.read()
    fp.close()
    out = json.loads(json_data)
    return out

def save_json(json_file,obj):
    full_path = os.path.expanduser(json_file)
    full_path = os.path.abspath(full_path)
    fp = open(full_path,"w")
    out = json.dumps(obj, indent=2)
    fp.write(out)
    fp.close()

class CAException(Exception):
    def __init__(self,val):
        self.val = val

    def __init__(self):
        return repr(self.val)

#remeber to use as_pem(cipher=None) when dumping an object to pem format
class CertificateAuthority(object):
    def __init__(self,*args,**kw):
        if kw.has_key("key_file"):
            self.key = self.load_pri_key(kw["key_file"])
        if kw.has_key("crt_file"):
            self.crt = self.load_crt(kw["crt_file"])

    def kw_subject(self,*args,**kw):
        subj = M2Crypto.X509.X509_Name()
        for k in ["CN","ST","L","O","OU","C","emailAddress","Email"]:
            if kw.has_key(k):
                setattr(subj,k,kw[k])
        return subj

    def loads_pri_key(self,keystr,**kw):
        if kw.has_key("callback"):
            return M2Crypto.RSA.load_key_string(keystr,callback=kw["callback"])
        else:
            return M2Crypto.RSA.load_key_string(keystr)

    def loads_req(self,csrstr):
        return M2Crypto.X509.load_request_string(csrstr)
        

    def load_req(self,file_path):
        req_pem = read_file(file_path)
        return self.loads_req(req_pem)

    def load_crt(self,file_path): 
        cert_pem = read_file(file_path)
        return self.loads_crt(cert_pem)


    def load_pri_key(self,file_path):
        pri_key_pem = read_file(file_path)
        return self.loads_pri_key(pri_key_pem)

    def loads_crt(self,cert_str):
        return M2Crypto.X509.load_cert_string(cert_str)

    def genkey(self,bits):
        pri_key = M2Crypto.RSA.gen_key(bits,65537,nop)
        return pri_key

    def gencsr(self,pri_key,*args,**kw):
        ca = kw.pop("ca",False)
        md = kw.pop("md","sha1")
        version = kw.pop("version",2)
        exts = M2Crypto.X509.X509_Extension_Stack()
        if ca:
            ext = M2Crypto.X509.new_extension('basicConstraints','CA:TRUE')
        else:
            ext = M2Crypto.X509.new_extension('basicConstraints','CA:FALSE')
        exts.push(ext)
        pubkey = M2Crypto.EVP.PKey()
        pubkey.assign_rsa(pri_key,capture=0)
        req = M2Crypto.X509.Request()
        req.add_extensions(exts)
        subj = self.kw_subject(**kw)
        req.set_subject(subj)
        req.set_pubkey(pubkey)
        req.set_version(version)
        req.sign(pubkey,md)
        return req      

    def signcsr(self,csr,*args,**kw):
        ca_key = self.key
        ca_crt = self.crt
        if ca_key == None or ca_crt == None:
            msg = "CA Cert and CA Key must be configured"
            raise CAException(msg)
        serial = kw.pop("serial",rnd.randint(0,2**31-1))
        ca = kw.pop("ca",False)
        md = kw.pop("md","sha1")
        version = kw.pop("version",2)
        days = kw.pop("days",730)
        secs = days * 24 * 60 * 60
        pubkey = csr.get_pubkey()
        if csr.verify(pubkey) != 1:
            msg = "X509 request's signature didn't match its own key"
            raise CAException(msg)
        if ca_crt.check_ca() == 0:
            raise CAException("ca certificate is not a signing cert")
        ext = self.caExt(ca)
        crt = M2Crypto.X509.X509()
        crt.set_pubkey(pubkey)
        crt.set_issuer(ca_crt.get_subject())
        crt.set_subject(csr.get_subject())
        (notbefore,notafter) = self.asn1Secs(secs)
        crt.add_ext(ext)
        crt.set_not_before(notbefore)
        crt.set_not_after(notafter)
        crt.set_serial_number(serial)
        crt.set_version(version)
        pri_evp = M2Crypto.EVP.PKey()
        pri_evp.assign_rsa(ca_key,capture=0)
        crt.sign(pri_evp,md)
        return crt
        
    def asn1Secs(self,secs):
        t = time.time()
        notbefore = M2Crypto.ASN1.ASN1_UTCTIME()
        notbefore.set_time(int(t))
        notafter = M2Crypto.ASN1.ASN1_UTCTIME()
        notafter.set_time(int(t+secs))
        return (notbefore,notafter)

    def caExt(self,isCa):
        if isCa:
            ext = M2Crypto.X509.new_extension('basicConstraints','CA:TRUE')
        else:
            ext = M2Crypto.X509.new_extension('basicConstraints','CA:FALSE')
        return ext
               
    def selfsignca(self,pri_key,csr,*args,**kw):
        serial = kw.pop("serial",rnd.randint(0,2**31-1))
        pubkey = csr.get_pubkey()
        days = kw.pop("days",730)
        md = kw.pop("md",'sha1')
        version = kw.pop("version",2)
        secs = days * 24 * 60 * 60
        ca = kw.pop("ca",False)
        if csr.verify(pubkey) != 1:
            msg = "X509 request's signature didn't match its own key"
            raise CAException(msg)
        name = csr.get_subject()
        (notbefore,notafter) = self.asn1Secs(secs)
        crt = M2Crypto.X509.X509()
        crt.set_not_before(notbefore)
        crt.set_not_after(notafter)
        crt.set_serial_number(serial)
        crt.set_pubkey(pubkey)
        name = csr.get_subject()
        crt.set_subject(name)
        crt.set_issuer(name)
        crt.set_version(version)
        crt.add_ext(self.caExt(ca))
        pri_evp = M2Crypto.EVP.PKey()
        pri_evp.assign_rsa(pri_key,capture=0)
        crt.sign(pri_evp,md)
        return crt
