#!/usr/bin/env jython

import java.net.URL as URL
import java.lang.Class as Class
import utils
import db

def getZeusTest(hid=None):
    conf = utils.load_json(utils.conf_file)
    endpoints = conf["zxtm"]["end_points"]
    user = conf["zxtm"]["user"]
    passwd = conf["zxtm"]["passwd"]
    default_ep_key = conf["zxtm"]["default_endpoint"]
    stubs = ZxtmStubs(endpoints,user,passwd)
    stubs.endpoint = stubs.endpoints[default_ep_key]
    return ZeusTest(stubs)


class ZeusTest(object):
    def __init__(self,zxtmStubs):
        self.stubs = zxtmStubs
        self.vs_names = None
        self.tg_names = None
        self.vs_tg = None
        self.vips = None
        self.pool_names = None

    def clear(self):
        self.vs_names = None
        self.tg_names = None
        self.vs_tg = None
        self.vips = None
        self.pool_names = None

    def getInfo(self):
        o = {}
        o["vs_names"]  = self.getVsNames()
        o["tg_names"] = self.getTGNames()
        o["vips"] = self.getVips()
        o["vs_vips"] = self.getVsTGNames()
        o["vs_ip"] = self.getVs2IpMap()
        o["ip_vs"] = inv_dict(o["vs_ip"])
        return o

    def getPoolNames(self):
        if self.pool_names != None:
            return self.pool_names
        self.pool_names = [pn for pn in self.stubs.p.getPoolNames()]
        self.pool_names.sort()
        return self.pool_names

    def getVsNames(self):
        if self.vs_names != None:
            return self.vs_names
        vs_names = [n for n in self.stubs.vs.getVirtualServerNames()]
        vs_names.sort()
        self.vs_names = vs_names
        return self.vs_names

    def getTGNames(self):
        if self.tg_names != None:
            return self.tg_names
        tg_names =  [n for n in self.stubs.tg.getTrafficIPGroupNames()]
        self.tg_names = tg_names
        return self.tg_names

    def getVsTGNames(self):
        if self.vs_tg != None:
            return self.vs_tg
        vsNames = self.getVsNames()
        ltg = self.stubs.vs.getListenTrafficIPGroups(vsNames)
        vtg = {}
        for(vsName,tgList) in zip(vsNames,ltg):
            if not vtg.has_key(vsName):
                vtg[vsName] = []
            for tg in tgList:
                vtg[vsName].append(tg)
        self.vs_tg = vtg
        return self.vs_tg

    def getVips(self):
        if self.vips != None:
            return self.vips
        vips = {}
        tg_names = self.getTGNames()
        result = self.stubs.tg.getIPAddresses(tg_names)
        for (tg_name,ipList) in zip(tg_names,result):
            for ip in ipList:
                if not vips.has_key(tg_name):
                    vips[tg_name] = []
                vips[tg_name].append(ip)
        self.vips = vips
        return self.vips

    def getVsIps(self):
        vips = self.getVips()
        vs_vips = self.getVips()
        

    def getVs2IpMap(self):
        vips = self.getVips()
        vs_tg = self.getVsTGNames()
        out = {}
        for(vs_name,tgList) in vs_tg.items():
            for tgName in tgList:
                if vips.has_key(tgName):
                    if not out.has_key(vs_name):
                        out[vs_name] = []
                    for ip in vips[tgName]:
                        out[vs_name].append(ip)
        return out

    def getCrtNames(self):
        names = [n for n in self.stubs.cert.getCertificateNames()]
        names.sort()
        return names


class ZxtmStubs(object):
    default_package = "com.zxtm.service.client"

    stubNames_default  = {
                           "ce":"ConfExtraBindingStub",
                           "p" :"PoolBindingStub",
                           "pc":"CatalogProtectionBindingStub",
                           "tg":"TrafficIPGroupsBindingStub",
                           "vs":"VirtualServerBindingStub",
                           "cert":"CatalogSSLCertificatesBindingStub",
                           "ca":"CatalogSSLCertificateAuthoritiesBindingStub"
                         }

    def __init__(self,endpoints,user,passwd,*args,**kw):
        self.stubNames = kw.get("stubNames",ZxtmStubs.stubNames_default)
        self.package = kw.get("package",ZxtmStubs.default_package)
        self.user = user
        self.passwd = passwd
        self.endpoints = endpoints
        endpointkeys = endpoints.keys()
        self.endpoint = endpoints[endpointkeys[0]]
        endpoint = self.endpoint
        self.stubs = {}
        self.setStubs()

    def setStubs(self):
        ep = self.endpoint
        self.stubs = {}
        for (shortName,stubClassName) in self.stubNames.items():
            fullClassName = "%s.%s"%(self.package,stubClassName)
            print "LoadingClass",fullClassName
            stubClass = Class.forName(fullClassName)
            ep_url = URL(ep)
            stubInstance = stubClass(ep_url,None)
            stubInstance.setUsername(self.user)
            stubInstance.setPassword(self.passwd)
            self.stubs[shortName] = stubInstance

    def setEndpoint(self,id):
        self.endpoint = self.endpoints[id]
        self.setStubs()

    def getMethods(self,stubName):
        out = []
        out = dir(self.stubs[stubName])
        out.sort()
        return out

    def __getattr__(self,shortName):
        if not self.stubs.has_key(shortName):
            raise AttributeError("'ZxtmStubs' has no attribute '%s'"%stubName)
        else:
            return self.stubs[shortName]

class Timer(object):
    def __init__(self):
        self.begin   = time.time()
        self.end   = time.time()
        self.stored  = 0.0
        self.stopped = True

    def restart(self):
        self.reset()
        self.start()


    def start(self):
        if not self.stopped:
            return 
        self.begin = time.time()
        self.stopped = False

    def stop(self):
        if self.stopped:
            return
        self.end = time.time()
        self.stored += self.end - self.begin
        self.stopped = True

    def read(self):
        if self.stopped:
             return self.stored
        now = time.time()
        total_time = now - self.begin + self.stored
        return total_time

    def reset(self):
        self.begin = time.time()
        self.end   = time.time()
        self.stored = 0.0

