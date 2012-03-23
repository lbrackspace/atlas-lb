#!/usr/bin/env python

import traceback
import datetime
import cPickle
import shelve
import copy
import time
import zlib
import stat
import sys
import os
import re


LEFT_DIR=1
RIGHT_DIR=2

BUFFSIZE = 1024*1024*64
COMPRESS=True

stat_file = "./pkhist.db"


valid_names = set(["qa","test","all","prod"])

def printf(format,*args): sys.stdout.write(format%args)
def fprintf(fp,format,*args): fp.write(format%args)

def test(re_pattern,l):
    rep = re.compile(re_pattern,re.IGNORECASE)
    m = rep.match(l)
    if not m:
        return False
    return m.groups()

def pad(digits,ch,val,**kargs):
  str_out=str(val)
  if not "side" in kargs:
    kargs["side"]=LEFT_DIR
  if kargs["side"]==LEFT_DIR or kargs["side"]=="LEFT_DIR":
    for i in xrange(0,digits-len(str_out)):
      str_out = ch + str_out
    return str_out
  if kargs["side"]==RIGHT_DIR or kargs["side"]=="RIGHT_DIR":
    for i in xrange(0,digits-len(str_out)):
      str_out = str_out + ch
    return str_out


def load_shelve(file_name=stat_file):
    return shelve.open(file_name,writeback=True)

def load_cpickle(file_name=stat_file,compress=COMPRESS):
    file_path = os.path.expanduser(file_name)
    data = open(file_name,"r",BUFFSIZE).read()
    if(compress):
        data = zlib.decompress(data)
    obj = cPickle.loads(data)
    return obj

def save_cpickle(obj,file_name=stat_file,compress=COMPRESS):
    ignoreCtrlC = True
    data = cPickle.dumps(obj)
    if(compress):
        data = zlib.compress(data,9)
    file_path = os.path.expanduser(file_name)
    while ignoreCtrlC:
       try:
           open(file_path,"w",BUFFSIZE).write(data)
           break
       except (KeyboardInterrupt,SystemExit):
           printf("KeyPress Ignored\n")
           continue
    return None

def mkdict(list_in,key):
    out = {}
    for obj in list_in:
        out[obj[key]] = obj
    return out

def sort_dict_list(list_in,*keys):
    temp_list = []
    for temp_dict in list_in:
        search_dict = temp_dict
        cell = []
        for key in keys:
            if key not in temp_dict:
                val = ""
            else:
                val = temp_dict[key]
            cell.append(val)
        temp_list.append((tuple(cell),temp_dict))
    temp_list.sort()
    list_out = [dict_obj[1] for dict_obj in temp_list]
    return list_out

def statfile(file_path):
    out = {}
    try:
        out["name"]=file_path
        out["isfile"]=os.path.isfile(file_path)
        out["isdir"]=os.path.isdir(file_path)
        out["stats"] = os.stat(file_path)
    except:
        return None
    return out

def rkeys(pstats,release):
    keys = pstats[release].keys()
    keys.sort()
    return keys

def dt2str(dateval):
    out  = ""
    ttup = dateval.timetuple()
    year = pad(4,'0',ttup[0])
    month = pad(2,'0',ttup[1])
    day = pad(2,'0',ttup[2])
    hr = pad(2,'0',ttup[3])
    mn = pad(2,'0',ttup[4]) 
    sc = pad(2,'0',ttup[5])
    return "%s-%s-%s-%s:%s:%s"%(year,month,day,hr,mn,sc)

def listdir(base,fstats):
    try:
        file_list = os.listdir(base)
    except (KeyboardInterrupt,SystemExit):
        raise
    except:
        return fstats
    for file_name in file_list:
        file_path = os.path.join(base,file_name)
        if not os.path.isdir(file_path) and not os.path.islink(file_path):
            try:
                fstats[file_path] = statfile(file_path)
                continue
            except (KeyboardInterrupt,SystemExit):
                raise
            except:
                print traceback.format_exc()
                continue
        fstats = listdir(file_path,fstats)
    return fstats

def getdebs(fstats):
    debs = {}
    for key in fstats.keys():
        m = deb_re.match(key)

        if not m and key.lower().endswith(".deb"):
            format = "Skiping marking of  %s it didn't match nameing scheme\n"
            printf(format,key)

        if m:
            dir_name = os.path.dirname(key)
            file_name = os.path.basename(key)
            pname = m.group(1)
            pver = m.group(2)
            hbuild = m.group(3)
            if not debs.has_key((pname,pver)):
                debs[(pname,pver)] = {}
            debs[(pname,pver)][hbuild]=key
    return debs

def difffiles(old_rev,new_rev):
    recent = {}
    old_fn = set(old_rev.keys())
    new_fn = set(new_rev.keys())
    remove_list = list(old_fn - new_fn)
    add_list = list(new_fn - old_fn)
    remove_list.sort()
    add_list.sort()

    for(k,tmp) in new_rev.items():
        stats = tmp["stats"]
        if not tmp["isfile"] or tmp["isdir"]:
            continue
        dirname = os.path.dirname(k)
        if not recent.has_key(dirname):
            recent[dirname]=(k,stats.st_mtime)
        else:
            if recent[dirname][1] > stats.st_mtime:
                recent[dirname]=(k,stats.st_mtime)

    recent_list = []
    for(k,v) in recent.items():
        recent_list.append(v[0])
    recent_list.sort()
    return (remove_list,add_list,recent_list)

def isodt2dt(isoStr):
    return dateutil.parser.parse(isoStr)

def dt2isodt(dt):
    return dt.isoformat()

def splitPstats(pstats,split_dt):
    pstatsOld = {}
    pstatsNew = {}
    for k in pstats.keys():
        pstatsOld[k]={}
        pstatsNew[k]={}
        for dt in pstats[k].keys():
            if dt < split_dt:
                pstatsOld[k][dt]=pstats[k][dt]
            else:
                pstatsNew[k][dt]=pstats[k][dt]
    return (pstatsOld,pstatsNew)

def mergePstats(ps1,ps2):
    pstats = {}
    for ps in [ps1,ps2]:
        for k in ps:
            if not pstats.has_key(k):
                pstats[k]={}
            for dt in ps[k].keys():
                pstats[k][dt] = ps[k][dt]
    return pstats

def getReleaseDates(pstats):
    releaseDates = {}
    for k in pstats.keys():
        releaseDates[k] = set(pstats[k].keys())
    return releaseDates


def now():
    return datetime.datetime.now()

class Timer(object):
    def __init__(self):
        self.begin   = time.time()
        self.end   = time.time()
        self.stored  = 0.0
        self.stopped = True

    def restart(self):
        self.reset()
        self.start()

    def start(self):
        if not self.stopped:
            return
        self.begin = time.time()
        self.stopped = False

    def stop(self):
        if self.stopped:
            return
        self.end = time.time()
        self.stored += self.end - self.begin
        self.stopped = True

    def read(self):
        if self.stopped:
             return self.stored
        now = time.time()
        total_time = now - self.begin + self.stored
        return total_time

    def reset(self):
        self.begin = time.time()
        self.end   = time.time()
        self.stored = 0.0

