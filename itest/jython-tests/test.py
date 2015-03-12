#!/usr/bin/env jython

import org.openstack.atlas.util.crypto.HashUtil as HashUtil
from zxtm import *
from utils import *
from db import *
from db_utils import *
app = getDb()
#db.buildClassImportFile("db_classes.py","local.json")

h = 2015010100
for i in xrange(0,1024):
    h = nextHour(h)
    print h


