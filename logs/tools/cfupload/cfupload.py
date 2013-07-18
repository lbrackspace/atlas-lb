#!/usr/bin/env python

import dbutils
import utils
import sys
import os
import re

lbid_re = re.compile("access_log_([0-9]+)_.*")

CONF_FILE = "~/cfupload.json"

def getLidAidNameFromLbs(con,cur):
    query = "select id,account_id,name from loadbalancer"
    cur.execute(query)
    result = cur.fetchall()
    rows = dbutils.getrows(cur,result)
    return rows

def mylistdir(*args):
    if len(args)==1:
        return os.listdir(args[0])
    return os.listdir(os.path.join(*args))

def scandir(cache_dir):
    file_list = []
    for date in mylistdir(cache_dir):
        for aid in mylistdir(cache_dir,date):
             for zip in mylistdir(cache_dir,date,aid):
                 file_path = os.path.join(cache_dir,date,aid,zip)
                 lid = ""
                 m = lbid_re.match(zip)
                 if m:
                     lid = m.group(1)
                 file_list.append([int(date),int(aid),int(lid),file_path])
    file_list.sort()
    return file_list

def joinscandirlbname(lbnamemap,scandir_list):
    out = {}
    for (date,aid,lid,file_path) in scandir_list:
        if not lbnamemap.has_key(lid):
            utils.printf("Warning lid[%i] was not found in lbmap\n",lid)
            continue
        name = lbnamemap[lid][2]
        if not out.has_key(aid):
            out[aid] = []
        dict_row = {"file_path" :file_path,
                    "aid":aid,
                    "lid":lid,
                    "lname":name,
                    "date":date}
        out[aid].append(dict_row)
    return out  

def scandir_filter(scandir_list,*args,**kw):
    out = []
    said = kw.get("aid",None)
    slid = kw.get("lid",None)
    sdgte = kw.get("date_gte",None)
    sdlte = kw.get("date_lte",None)
    sdate = kw.get("date",None)

    if kw.has_key("eaids"):
        eaids = splitIntSet(kw.get("eaids"))
    else:
        eaids = set([])

    if kw.has_key("elids"):
        elids = splitIntSet(kw.get("elids"))
    else:
        elids = set([])
    
    for file_entry in scandir_list:
        (date,aid,lid,full_path) = file_entry
        if len(eaids) > 0 and aid in eaids:
            continue
        if len(elids) > 0 and lid in elids:
            continue 
        if said != None and said != aid:
            continue
        if slid != None and slid != lid:
            continue
        if sdate != None and date != sdate:
            continue
        if sdlte != None and date > sdlte:
            continue
        if sdgte != None and date < sdgte:
            continue
        out.append(file_entry)
    return out  

def splitIntSet(val):
    intSet = set()
    try:
        n = int(val)
        intSet.add(n)
    except (ValueError):
        strList = val.split(",")
        for strVal in strList:
            try:
                n = int(strVal)
                intSet.add(n)
            except ValueError:
                continue
            
    return intSet

def getLbid2name(lbRows):
    out = {}
    for row in lbRows:
        aid = int(row["account_id"])
        lid = int(row["id"])
        name = row["name"]
        out[lid]=(aid,lid,name)
    return out

def getCfFiles(*args,**kw):
    conf_file = kw.pop("conf_file",CONF_FILE)
    conf = utils.load_json(conf_file)
    mysql = conf["mysql"]
    cache_dir = conf["cache_dir"]
    (con,cur) = dbutils.getConCur(**mysql)
    lbRows = getLidAidNameFromLbs(con,cur)
    lb2name = getLbid2name(lbRows)
    cur.close()
    con.close()
    scan_list = scandir_filter(scandir(cache_dir),**kw)
    cfFiles = joinscandirlbname(lb2name,scan_list)
    return (scan_list,cfFiles)

def main(args):
    kw = {}
    for arg in args[1:]:
        (k,v) = arg.split("=")
        try:
            kw[k.strip()] = int(v.strip())
        except ValueError:
            kw[k.strip()] = v.strip() #if its not an int treat it like a string
    return getCfFiles(**kw)

if __name__ == "__main__":
    (scan_list,cfFiles) = main(sys.argv)
    for i in xrange(0,len(scan_list)):
        utils.printf("scan_list[%i]=%s\n",i,scan_list[i])
    for(aid,files) in cfFiles.items():
        for file_info in files:
            utils.printf("%s: %s\n",aid,file_info)

#example invocation
#./cfupload.py aid=452605 date_gte=2013060600
#./cfupload.py aid=452605
#/cfupload.py aid=682644 date_gte=2013052500 date_lte=2013052523
