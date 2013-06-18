#!/usr/bin/env python

import json
import sys
import os

def printf(format,*args): sys.stdout.write(format%args)

def fprintf(fp,format,*args): fp.write(format%args)

def load_json(json_file):
    json_data = open(fullPath(json_file),"r").read()
    return json.loads(json_data)

def save_json(json_file,obj):
    json_data = json.dumps(obj, indent=2)
    open(fullPath(json_file),"w").write(json_data)

def fullPath(path_in):
    return os.path.abspath(os.path.expanduser(path_in))


def pad(val_str,digits):
    return "0"*(digits-len(val_str))+val_str
