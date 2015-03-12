from utils import *
from db import *
from db_classes import *
import datetime
import org.openstack.atlas.util.crypto.HashUtil as HashUtil

jan1 = datetime.datetime(2015,1,1,0,0)

def newHdfsLzo(hour):	
    hl = HdfsLzo(hour)
    return hl

def newCloudFilesLzo(hour, frag=0, md5=""):
    cl = CloudFilesLzo(hour, frag, md5)
    return cl

def dt_long(dt):
    val = dt.year * 1000000 + dt.month * 10000 + dt.day * 100 + dt.hour
    return val

def hour_key(nHours):
    dt = jan1 + datetime.timedelta(hours=nHours)
    return dt_long(dt)

def new_lzo(nHours,frags):
    lzo = []
    for h in xrange(0,nHours):
        hk = hour_key(h)
        lzo.append(newHdfsLzo(hk))
        for f in xrange(0,frags):
            valformd5 = HashUtil.sha1sumHex("%d:%d"%(hk,f))
            lzo.append(newCloudFilesLzo(hk,frag=f,md5=valformd5))
    return lzo

def getHdfsLzo(hour):
    resp = qq("from HdfsLzo where hour_key =%s"%hour)
    l = len(resp)
    if l != 1:
        print "Error resp len = %d"%l
    return resp[0]

def getCloudLzo(hour,frag):
    resp = qq("from HdfsLzo where hour_key =%s"%hour)
    l = len(resp)
    if l != 1:
        print "Error resp len = %d"%l
    return resp[0]

def allUnfinished():
    lzo = []
    resp = qq("select h from HdfsLzo h where h.finished=false")
    lzo.extend(resp)
    resp = qq("select c from CloudFilesLzo c where c.finished=false")
    lzo.extend(resp)
    return lzo

def finishLzo(lzo_list, nFreq, val=True):
    i = 0
    out = []
    for l in lzo_list:
        i += 1
        if i >= nFreq:
            i = 0
            l.setFinished(val)
            out.append(l)
    return out

