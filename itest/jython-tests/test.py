#!/usr/bin/env jython

import org.openstack.atlas.util.crypto.HashUtil as HashUtil
from zxtm import *
from utils import *
from db import *
from db_utils import *
app = getDb()
#db.buildClassImportFile("db_classes.py","local.json")

begin()
lzo = new_lzo(31*6,16)
save_list(lzo)
commit()


