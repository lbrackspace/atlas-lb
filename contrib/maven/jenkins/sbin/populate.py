#!/usr/bin/env python

import traceback
import datetime
import stat
import cPickle
import string
import time
import zlib
import json
import sys
import os
import re

BLOCKSIZE = 1024*1024

snapshot_re = re.compile(".*snapshot.*\.deb$",re.IGNORECASE)
package_re = re.compile("([^_]*)_.*\.deb$",re.IGNORECASE)
migration_re = re.compile("[0-9]+-[0-9]+.sql",re.IGNORECASE)

def fprintf(fp,format,*args): fp.write(format%args)

def printf(format,*args):
    now = datetime.datetime.now().__str__()
    sys.stdout.write(format%args)
    msg = format%args
    lfp.write("%s: %s"%(now,msg))
    lfp.flush()

def test_re(pattern,text):
    pattern_re = re.compile(pattern)
    m = pattern_re.match(text)
    if m:
        return m.groups()
    else:
        return False

def splitpath(path):
    full_path = fullpath(path)
    return full_path.split(os.path.sep)

def joinpath(components):
    return string.join(components,os.path.sep)

def save_cpickle(obj,file_name,compress=False):
    ignoreCtrlC = True
    data = cPickle.dumps(obj)
    if(compress):
        data = zlib.compress(data,9)
    file_path = os.path.expanduser(file_name)
    while ignoreCtrlC:
       try:
           open(file_path,"w",BLOCKSIZE).write(data)
           break
       except (KeyboardInterrupt,SystemExit):
           printf("KeyPress Ignored\n")
           continue
    return None

def load_json(file_name):
    file_path = os.path.expanduser(file_name)
    fp = open(file_path,"r")
    jtext = fp.read()
    fp.close()
    obj = json.loads(jtext)
    return obj

def save_json(obj,file_name):
    file_path = os.path.expanduser(file_name)
    try:
        jtext = json.dumps(obj,indent=2)
    except:
        printf("Error %s\n",traceback.format_exc())
        sys.exit()
    fp = open(file_path,"w")
    fp.write(jtext)
    fp.close()

def load_cpickle(file_name,compress=False):
    file_path = os.path.expanduser(file_name)
    data = open(file_path,"r",BLOCKSIZE).read()
    if(compress):
        data = zlib.decompress(data)
    obj = cPickle.loads(data)
    return obj

def stripbasedir(basedir,fulldir):
    basedir_components = splitpath(basedir)
    fulldir_components = splitpath(fulldir)
    stripeddir_components = fulldir_components[len(basedir_components):]
    striped_dir = joinpath(stripeddir_components)
    return striped_dir

def fullpath(path_in):
    return os.path.abspath(os.path.expanduser(path_in))

def fdir():
    (r,w,fr,fw) = npipe()
    this_dir = fullpath(os.getcwd())
    pid = os.fork()
    if pid == -1:
        printf("Fork error\n")
        yeild
    elif pid == 0:
        fr.close()
        listdir(fp=fw)
        fw.close()
        sys.exit()
    else:
        fw.close()
        while True:
            line = fr.readline()
            if len(line)<=0:
                break
            striped_dir = stripbasedir(this_dir,chop(line))
            yield striped_dir
        fr.close()
        os.waitpid(pid,0) #Wait for the child process to die
    os.chdir(this_dir)

def listdir(fp=sys.stdout,onlydir=False):
    cur_path=os.getcwd()
    dir_list=os.listdir(".")
    for entry in dir_list:
        os.chdir(cur_path)
        if os.path.isdir(entry) and not os.path.islink(entry):
           if onlydir:
               fp.write("%s\n"%os.path.abspath(entry))
               fp.flush()
           try:
              os.chdir(entry)
              listdir(fp=fp,onlydir=onlydir)
              os.chdir("..")
           except (KeyboardInterrupt,SystemExit):
              raise
           except:
              printf("Error entering \"%s\" directory\n",entry)
              continue
        else:
            if not onlydir:
                msg = "%s%s%s\n"%(cur_path,os.path.sep,entry)
                fp.write(msg)
                fp.flush()

def chop(line):
    return line.replace("\r","").replace("\n","")

def npipe():
    (r,w) = os.pipe()
    (fpr,fpw) = (os.fdopen(r,"r"),os.fdopen(w,"w"))
    return (r,w,fpr,fpw)

def copy_file(src,dst,blocksize=BLOCKSIZE):
    printf("Adding %s: ",os.path.basename(dst))
    #printf("%s -> %s\n",src,dst)
    sys.stdout.flush()
    fi = open(src,"r",blocksize)
    fo = open(dst,"w",blocksize)
    while True:
        data = fi.read(blocksize)
        if len(data)<=0:
            break
        fo.write(data)
        fprintf(sys.stdout,".")
        sys.stdout.flush()
    printf("\n")
    sys.stdout.flush()
    fi.close()
    fo.close()

def isdeb(file_name):
    if os.path.splitext(file_name)[1].lower() == ".deb":
        return True
    else:
        return False

def run(cmd):
    printf("EXC: %s\n",cmd)
    fp = os.popen(cmd,"r")
    for line in fp.xreadlines():
        sys.stdout.write(line)
    sys.stdout.flush()

def pool_name(basedir,dst):
    deb = os.path.basename(dst)
    m = package_re.match(deb)
    if not m:
        printf("Coulden't mangle %s to a proper package name\n",deb)
        package_name="unknown"
    else:
        package_name = m.group(1)
    letter = deb[0]
    out_path = os.path.join(basedir,letter,package_name,deb)
    return out_path

def pad(digits,ch,val,**kargs):
    str_out=str(val)
    if not "side" in kargs:
        kargs["side"]="LEFT_DIR"
    if kargs["side"]=="LEFT_DIR" or kargs["side"]=="LEFT":
        for i in xrange(0,digits-len(str_out)):
            str_out = ch + str_out
        return str_out
    if kargs["side"]=="RIGHT_DIR" or kargs["side"]=="RIGHT":
        for i in xrange(0,digits-len(str_out)):
            str_out = str_out + ch
        return str_out

def makedirp(dirname):
    try:
        os.makedirs(dirname)
        return
    except OSError:
        return

if __name__ == "__main__":
    lfp = open(fullpath("~/populate.log"),"a")
    printf("Build started in dir %s\n",os.getcwd())
    cnf = load_json("~/populate.json")
    topdir = cnf["topdir"]
    incoming = os.path.join(topdir,cnf["incoming"])
    qa_up = cnf["qa_up"]
    populate_cmd = cnf["populate_cmd"]
    blocksize = cnf["blocksize"]
    pool = os.path.join(topdir,cnf["pool"])
    deb_files = []
    for file_path in fdir():
        if not isdeb(file_path):
            continue
        if snapshot_re.match(file_path):
            printf("skipping %s\n",file_path)
        else:
            deb_files.append(file_path)

    if len(deb_files)>0:
        deb_files.sort()
        for deb_file in deb_files:
            src = deb_file
            dst = pool_name(pool,src)
            try:
                makedirp(os.path.dirname(dst))
                copy_file(src,dst,blocksize=blocksize)
            except KeyboardInterrupt:
                printf("KeyboardInterrupt received exiting\n")
                sys.exit()
            except:
                msg = traceback.format_exc()
                printf("Error copying: %s -> %s:%s \n",src,dst,msg)
                continue
        run(populate_cmd)
        run(qa_up)
    else:
        printf("No deb packages found. Not populating\n")
    printf("DONE\n\n")
    lfp.close()
