#!/usr/bin/env jython

import util
util.setDbConfig("./local.json")
from util import *

begin()

lbs = LzoFakerMain.getActiveLoadbalancerIdsAndNames(app)

commit()

