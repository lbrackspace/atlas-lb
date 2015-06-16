#!/usr/bin/env jython
	
import util
util.setConfig("ndev.json")
from util import *

begin()

qStr = loadbalancerWithPTREvents()
