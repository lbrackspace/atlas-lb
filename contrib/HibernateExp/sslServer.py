#!/usr/bin/env jython

import org.openstack.atlas.util.ca.primitives.RsaConst as RsaConst
import org.openstack.atlas.util.ca.primitives.RsaPair as RsaPair
import org.openstack.atlas.util.ca.RSAKeyUtils as RSAKeyUtils
import org.openstack.atlas.util.ca.CertUtils as CertUtils
import org.openstack.atlas.util.ca.PemUtils as PemUtils
import org.openstack.atlas.util.ca.CsrUtils as CsrUtils
import java.lang.String as String

import simplejson as json
import SimpleXMLRPCServer
import traceback
import sys
import os


def printf(format,*args): sys.stdout.write(format%args)

def fprintf(fp,format,*args): fp.write(format%args)


def fullPath(file_path):
    full_path = os.path.expanduser(file_path)
    full_path = os.path.abspath(full_path)
    return full_path


def load_json(json_file):
    fp = open(fullPath(json_file),"r")
    json_data = fp.read()
    fp.close()
    out = json.loads(json_data)
    return out

def save_json(json_file,obj):
    fp = open(fullPath(json_file),"w")
    out = json.dumps(obj, indent=2)
    fp.write(out)
    fp.close()

def save_cpickle(pickle_file,obj):
    data = cPickle.dumps(obj)
    fp = fullOpen(pickle_file,"w")
    fp.write(data)
    fp.close()

def load_cpickle(pickle_file):
    fp = fullOpen(pickle_file,"r")
    data = fp.read()
    fp.close()
    obj = cPickle.loads(data)
    return obj

def usage(prog):
    printf("usage is %s <host|ip> <port>\n",prog)
    printf("\n")
    printf("Spins up an XML rpc server for ssl stuff\n")

def excuse():
    (t,v,s) = sys.exc_info()
    eList= traceback.format_exception(t,v,s)
    return eList

def str2bytes(strIn,encode="US-ASCII"):
    jStr = String(strIn) #Convert it to a java string if its not already
    bytes = jStr.getBytes(encode)
    return bytes
    

def bytes2str(bytes,decode="US-ASCII"):
    strOut = "%s"%String(bytes,decode) #To be symetric convert back to jython string :(
    return strOut

def fromPemStr(strIn):
    bytes = str2bytes(strIn)
    obj = PemUtils.fromPem(bytes)
    return obj

def toPemBytes(obj):
    if isinstance(obj,RsaPair):
        bytes = PemUtils.toPem(obj.toJavaSecurityKeyPair())
    else:
        bytes = PemUtils.toPem(obj)
    return "%s"%String(bytes,"US-ASCII")

def toPemStr(obj):
    bytes = toPemBytes(obj)
    strOut = bytes2str(bytes)
    return strOut

def newCrtBytes(bits,subj,caKey,caCrt,**kw):
    certainity = kw.pop("certainity",32)
    key = RSAKeyUtils.genRSAPair(bits,certainity)
    csr = CsrUtils.newCsr(subj,key,False)
    crt = CertUtils.signCSR(csr,caKey,caCrt,730,None)
    return (key,csr,crt)

def subjStr(subjDict):
    out = ""
    subjList = [(k,v) for (k,v) in subjDict.items()]
    for (k,v) in subjList[:-1]:
        out += "%s=%s,"%(k,v)
    (k,v) = subjList[-1]
    out += "%s=%s"%(k,v)
    return out

class CaServer(object):
    def echo(self,str_in):
        return "echo: %s"%str_in

    def newCrt(self,bits,subjStrIn,caKeyStr,caCrtStr):
        caKey = RsaPair(fromPemStr(caKeyStr))
        caCrt = fromPemStr(caCrtStr)
        subj = subjStr(subjStrIn)
        (key,csr,crt) = newCrtBytes(bits,subj,caKey,caCrt,certainity=32)
        (keyStr,csrStr,crtStr) = (toPemStr(key),toPemStr(csr),toPemStr(crt))
        return (keyStr,csrStr,crtStr)

if __name__ == "__main__":
    prog = os.path.basename(sys.argv[0])
    Server = SimpleXMLRPCServer.SimpleXMLRPCServer
    Handler = SimpleXMLRPCServer.SimpleXMLRPCRequestHandler
    conf_file = "/etc/sslServer.json"
    printf("reading configuration from %s\n",conf_file)
    conf = load_json(conf_file)

    host = conf["host"]
    port = int(conf["port"])

    caServer = CaServer()
    try:
        printf("binding to %s:%s\n",host,port)
        s = Server((host,port),Handler,allow_none=True)
        s.register_instance(caServer)
    except:
        printf("Error starting server: %s\n",excuse())
        sys.exit()
    try:
        s.serve_forever()
    except:
        printf("Exiting due to except: %s\n",excuse())
        sys.exit()
