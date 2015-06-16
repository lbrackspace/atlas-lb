#!/usr/bin/env jython

from util import *
import traceback
import sys

if len(sys.argv) < 2:
    config_file = "slice.xml"
else:
    config_file = sys.argv[1]

try:
    setConfig(config_file) #Runs the hibernate validater
except e:
    print "%s\nFAIL"%traceback.format_exc()
    sys.exit()


