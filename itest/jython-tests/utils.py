#!/usr/bin/env jython

import org.openstack.atlas.util.staticutils.StaticDateTimeUtils as StaticDateTimeUtils
import org.openstack.atlas.util.staticutils.StaticFileUtils as StaticFileUtils
import org.openstack.atlas.util.staticutils.StaticStringUtils as StaticStringUtils
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils as StaticDateTimeUtils
import org.openstack.atlas.util.debug.Debug as Debug

import com.xhaus.jyson.JysonCodec as json

import java.util.List as List
import java.util.ArrayList as ArrayList
import java.lang.String as String

import pickle

import sys
import os

conf_file = "./stag.json"

def setConfFile(filePath):
    global conf_file
    conf_file = filePath

def printf(format,*args):
    sys.stdout.write(format%args)

def fprintf(fp,format,*args):
    fp.write(format%arg)

def fullPath(*pathcomps):
    file_path = os.path.join(*pathcomps)
    return os.path.expanduser(file_path)

def save_json(file_path,obj):
    jStr = json.dumps(obj)
    fp = open(fullPath(file_path),"w")
    fp.write(jStr)
    fp.close()

def load_json(file_path):
    fp = open(fullPath(file_path),"r")
    jsonStr = fp.read()
    fp.close()
    jsonObj = json.loads(jsonStr)
    return pickle.loads(pickle.dumps(jsonObj)) #poor way to strip stringmap

