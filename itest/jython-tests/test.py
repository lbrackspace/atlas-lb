#!/usr/bin/env jython

import org.openstack.atlas.util.crypto.HashUtil as HashUtil
from zxtm import *
from utils import *
from db import *
from db_utils import *
app = getDb()
#db.buildClassImportFile("db_classes.py","local.json")
from db_classes import *

h = []
for i in xrange(0, 1024*1024):
    h.append(newHdfsLzo(i))
    if i%1024==0:
         print i/1024

for i in xrange(0,1024):
    print i
    for j in xrange(0,1024):
        h.append(newCloudFilesLzo(i,frag=j))

save_list(h)













a = Account()
a.setId(1)
sha1=HashUtil.sha1sumHex("1",0,4)
a.setSha1SumForIpv6(sha1)
a.setClusterType(ClusterType.INTERNAL)


zt = getZeusTest(hid=1)
