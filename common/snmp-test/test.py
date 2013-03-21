#!/usr/bin/env jython

import util
from util import *

(ip,port,community,maxReps,nonReps)=("127.0.0.1","1161","public")

ssc = StingraySnmpClient(ip,port,community)

