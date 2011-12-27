#!/usr/bin/env python

# SSL wrap around stolen from  
# http://aspn.activestate.com/ASPN/Cookbook/Python/Recipe/496786/index_txt

DAEMON = True
DEBUG = True

from certificateauthority import *

from OpenSSL import SSL
import SimpleXMLRPCServer
import SimpleHTTPServer
import BaseHTTPServer
import SocketServer
import xmlrpclib
import traceback
import datetime
import OpenSSL
import string
import socket
import signal
import json
import copy
import time
import sys
import os
import re

def printf(format,*args): sys.stdout.write(format%args)

def fprintf(fp,format,*args): fp.write(format%args)

def has_keys(dict_in,keys):
    for key in keys:
        if key not in dict_in:
            return False
    return True

def test_re(regex,lines):
    out = []
    tre = re.compile(regex)
    for line in lines:
        m = tre.match(line)
        if m:
            out.append(m.groups())
    return out

def chop(line_in):
      line_out=line_in;
      line_out=line_out.replace("\n","")
      line_out=line_out.replace("\r","")
      return(line_out)

def pad(digits,ch,val,**kargs):
    str_out=str(val)
    if not "side" in kargs:
        kargs["side"]="LEFT_DIR"
    if kargs["side"]=="LEFT_DIR" or kargs["side"]=="LEFT":
        for i in xrange(0,digits-len(str_out)):
            str_out = ch + str_out
        return str_out
    if kargs["side"]=="RIGHT_DIR" or kargs["side"]=="RIGHT":
        for i in xrange(0,digits-len(str_out)):
            str_out = str_out + ch
        return str_out

def excuse():
    (t,v,s) = sys.exc_info()
    eList= traceback.format_exception(t,v,s)
    return eList

def printexcuse(text):
    lf.write(text)
    lf.flush()

def version():
    return sys.version_info[0:3]

def closepid(signum,frame):
    try:
        fprintf(lf,"Exiting\n")
        os.unlink(lock_path)
    except:
        fprintf(lf,"Error unlinking %s\n",lock_path)
        fprintf(lf,"Excuse is %s\n",excuse())
        fprintf(lf,"\n\n\n")
        fprintf(lf,"No worrys exiting anyways\n")
        lf.flush()
    sys.exit()

def load_json(file_name):
    file_path = os.path.expanduser(file_name)
    jsonStr = open(file_path).read()
    config = json.loads(jsonStr)
    return config 


def save_json(file_name,config):
    file_path = os.path.expanduser(file_name)
    jsonStr = json.dumps(config,indent=4)
    open(file_path,"w").write(jsonStr)

def bury_the_dead():
  while True:
    try:
      (pid,stats) = os.waitpid(-1,os.WNOHANG)
      if (pid,stats) == (0,0):
        break
    except:
      break

def missing_keys(required_keys,keys_found):
    required_set = set(required_keys)
    found_set    = set(keys_found)
    return list(required_set - found_set)

def now():
   return datetime.datetime.now()

def fullPath(file_path):
    return os.path.abspath(os.path.expanduser(file_path))

class FuncXMLRPCError(Exception):
    def __init__(self,value):
        self.value = value

    def __str__(self):
      return repr(self.value)

class AuthException(Exception):
    def __init__(self,val):
        self.val = val;

    def __str__(self):
        return repr(self.val)
def Auth(func):
    def auth_decorator(*args,**kwargs):
        INVCRED = "Invalid Credentials"
        NOCRED  = "You forgot to pass your credentials "
        NOCRED += "as the first argument"
        if len(args) < 2:
            raise AuthException(NOCRED)
        self = args[0]
        cred = args[1]
        if type(cred)!=type({}) or not has_keys(cred,["user","passwd"]):
            raise AuthException(INVCRED)
        if cred["user"] != self.cred["user"]:
            raise AuthException(INVCRED)
        if cred["passwd"] != self.cred["passwd"]:
            raise AuthException(INVCRED)
        nargs = args[0:1] + args[2:]
        return func(*nargs,**kwargs)
    return auth_decorator


class SecureXMLRPCServer(SocketServer.ForkingMixIn,
                         BaseHTTPServer.HTTPServer,
                         SimpleXMLRPCServer.SimpleXMLRPCDispatcher):

    def __init__(self, server_address, HandlerClass, logRequests=True):
        """Secure XML-RPC server.

        It it very similar to SimpleXMLRPCServer but it uses HTTPS for transporting XML data.
        """
        self.logRequests  = logRequests

        pyversion = version()

        if pyversion < (2,5,0) and pyversion >= (2,4,0):
            SimpleXMLRPCServer.SimpleXMLRPCDispatcher.__init__(self)

        elif pyversion >= (2,5,0):
            SimpleXMLRPCServer.SimpleXMLRPCDispatcher.__init__(self,False,None)
          
        else:
            raise " python version needs to be in  2.4.0 =< v < 2.6.0 "
            #Anybodys guess
        self.max_children = MAXCHILD
        SocketServer.BaseServer.__init__(self, server_address, HandlerClass)
        ctx = SSL.Context(SSL.SSLv23_METHOD)
        ctx.use_privatekey_file (KEYFILE)
        ctx.use_certificate_file(CERTFILE)
        self.socket = SSL.Connection(ctx, socket.socket(self.address_family,
                                                        self.socket_type))
        self.server_bind()
        self.server_activate()

class SecureXMLRpcRequestHandler(SimpleXMLRPCServer.SimpleXMLRPCRequestHandler):
    """Secure XML-RPC request handler class.

    It it very similar to SimpleXMLRPCRequestHandler but it uses HTTPS for transporting XML data.
    """

    def setup(self):
        self.connection = self.request
        self.rfile = socket._fileobject(self.request, "rb", self.rbufsize)
        self.wfile = socket._fileobject(self.request, "wb", self.wbufsize)
        
    def do_POST(self):
        """Handles the HTTPS POST request.

        It was copied out from SimpleXMLRPCServer.py and modified to shutdown the socket cleanly.
        """

        try:
            # get arguments
            data = self.rfile.read(int(self.headers["content-length"]))
            # In previous versions of SimpleXMLRPCServer, _dispatch
            # could be overridden in this class, instead of in
            # SimpleXMLRPCDispatcher. To maintain backwards compatibility,
            # check to see if a subclass implements _dispatch and dispatch
            # using that method if present.
            response = self.server._marshaled_dispatch(
                    data, getattr(self, '_dispatch', None)
                )
          #Forked Children Need to die so we should not respond to a
          #SystemExit
        except (KeyboardInterrupt,SystemExit):
          if DEBUG:
              fprintf(lf,"pid[%i]Died in do_post\n",os.getpid())
          sys.exit()
        except: # This should only happen if the module is buggy
            # internal error, report as HTTP server error
            if DEBUG:
                fprintf(lf,"Excuse is %s\n",excuse())
            self.send_response(500)
            self.end_headers()
        else:
            # got a valid XML RPC response
            self.send_response(200)
            self.send_header("Content-type", "text/xml")
            self.send_header("Content-length", str(len(response)))
            self.end_headers()
            self.wfile.write(response)

            # shut down the connection
            self.wfile.flush()
            self.connection.shutdown() # Modified here!

class CaServer(object):
    def __init__(self,cred,ca_keyfile,ca_crtfile,serial):
        self.cred = cred
        self.ca = CertificateAuthority(key_file=ca_keyfile,crt_file=ca_crtfile)
        self.serial=serial
        

    @Auth
    def echo(self,str_in):
        return "echo: %s"%str_in

    @Auth
    def genCrt(self,key_bits,subj):
        out = {"key":"","csr":"","crt":"","error":""}
        try:
            key = self.ca.genkey(key_bits)
            out["key"] = key.as_pem(cipher=None)
            csr = self.ca.gencsr(key,**subj)
            out["csr"] = csr.as_pem()
            crt = self.ca.signcsr(csr,days=1825)
            out["crt"] = crt.as_pem()
        except Exception, ex:
            (t,v,s) = sys.exc_info()
            eList = traceback.format_exception(t,v,s)
            out["error"] = eList
            for entry in eList:
                lf.write(entry)
                lf.flush()
            return out
        return out


if __name__ == "__main__":
    log_file = sys.argv[0]
    log_file = os.path.basename(log_file)        #strip off the leading path
    log_file = os.path.splitext(log_file)[0]     #strip off the .py
    log_file = os.path.join("./",log_file) #Then point it to the log dir
    log_file += ".log"                           #Oh yea put .log on the end

    prog = os.path.basename(sys.argv[0])         #programe name

    if len(sys.argv)<2:
        config_file = os.path.splitext(prog)[0] + ".json"
        config_file = os.path.join("./",config_file)
    else:
        config_file = sys.argv[1]

    pid_name = os.path.basename(sys.argv[0]) + ".pid"
    lock_path = os.path.join("/var/run",pid_name)

    if DEBUG:
        printf("Useing \"%s\" as config file\n",config_file)
        sys.stdout.flush()

    try:
        config = load_json(config_file)
    except:
        try:
           msg="Coulden't read config_file %s excuse %s\n"%(config_file,excuse())
           printf("%s",msg)
           lfp = open(log_file,"a")
           fprintf(lfp,"%s",msg)
           lfp.close()
           sys.exit()
        except IOError:
           printf("Couden't open log file either %s reason%s\n",log_file,excuse())
           sys.exit()

    #Append the extra config options you want here
    required_keys = [ "cert" , "key" , "port" ,"max_child","passwd"]

    missing_variables = missing_keys(required_keys,config.keys())


    if len(missing_variables) > 0:
        printf("Error missing config variables:\n")
        for missing_var in missing_variables:
            printf("%s = <insert value here>\n",missing_var)
        printf("Please add the above lines to your config file\n")
        sys.exit()

    if "host" in config:
        HOST = config["host"]
    else:
        HOST = socket.gethostname()

    KEYFILE         = fullPath(config["key"])
    CERTFILE        = fullPath(config["cert"])
    PORT            = config["port"]
    MAXCHILD        = config["max_child"]
    CRED            = {"user":config["user"],
                       "passwd":config["passwd"]}

    #Insert your other global variable options here
    CA_KEYFILE = fullPath(config["ca_keyfile"])
    CA_CRTFILE = fullPath(config["ca_crtfile"])

    if DEBUG:
        printf("%s started at %s\n",os.path.basename(sys.argv[0]),time.asctime())
        printf("Usering config\n")
        printf("KEYFILE = %s\n",KEYFILE)
        printf("CERTFILE = %s\n",CERTFILE)
        printf("HOST = %s\n",HOST)
        printf("PORT = %s\n",PORT)
        printf("MAXCHILD  = %i\n",MAXCHILD)
        sys.stdout.flush()


    #start the SecureXMLRPC
    Handler = SecureXMLRpcRequestHandler # Alias this big ass name
    Server  = SecureXMLRPCServer         # Alias this one too

    #In order to be a real daemon process we need to fork off twice 
    #aparently

    if DAEMON:
        printf("DAEMON MODE\n")


        if os.fork():
            os._exit(0)

        os.setsid()

        if os.fork():
            os._exit(0)

        os.chdir("/")
        os.umask(0)

        try:
            lf = open(log_file,"a")
            fprintf(sys.stderr,"Useing %s for log file\n",log_file)
        except:
            user_dir = os.path.expanduser("~/")
            new_file = os.path.basename(log_file)
            log_path = os.path.join(user_dir,new_file)
            fprintf(sys.stderr,"Error opening %s for logging\n",log_path)
            fprintf(sys.stderr,"Useing %s instead\n",log_path)
            try:
                lf = open(log_path,"a")
            except:
                fprintf(sys.stderr,"Ok guess that didn't work either\n")
                fprintf(sys.stderr,"Useing /dev/null\n")
                lf = open("/dev/null","w")           

   

        #close terminal file pointers
        si = file("/dev/null", 'r')
        so = file("/dev/null", 'a+')
        se = file("/dev/null", 'a+', 0)
        os.dup2(si.fileno(), sys.stdin.fileno())
        os.dup2(so.fileno(), sys.stdout.fileno())
        os.dup2(se.fileno(), sys.stderr.fileno())    

        fprintf(lf,"%s[%i] started at %s\n",prog,os.getpid(),time.asctime())
        lf.flush()
    else:
        lf = sys.stderr

    #log pid file
    try:
        fp = open(lock_path,"w")
        fp.write("%s"%os.getpid())
        fp.close()
    except:
        fprintf(lf,"Error writing to \"%s\"\n",lock_path)
        fprintf(lf,"Excuse is %s\n",excuse())
        lf.flush()

    try:
        caServer = CaServer(CRED,CA_KEYFILE,CA_CRTFILE,2)
        fprintf(lf,"bound to host=%s port =%s\n",HOST,PORT)
        lf.flush()
        signal.signal(signal.SIGTERM,closepid)
        s = Server((HOST,PORT),Handler)
        s.register_instance(caServer,allow_dotted_names=True)
    except:
        msg = "Error starting server reason\nExcuse: %s\n"%excuse()
        fprintf(lf,"%s",msg)
        sys.exit()

    try:
        s.serve_forever()
    except:
        closepid(None,None)
        sys.exit()
