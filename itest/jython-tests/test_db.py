#!/usr/bin/env jython

import org.openstack.atlas.util.crypto.HashUtil as HashUtil
import utils
from zxtm import *
from utils import *
from db import *

setConfFile("./dev.json")

app = getDb()
#db.buildClassImportFile("db_classes.py",utils.conf_file)
zt = getZeusTest(hid=1)

from db_classes import *

#a = Account()
#a.setId(1)
#sha1=HashUtil.sha1sumHex("1",0,4)
#a.setSha1SumForIpv6(sha1)
#a.setClusterType(ClusterType.INTERNAL)


