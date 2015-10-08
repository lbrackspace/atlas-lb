#!/usr/bin/env python

import json
import sys
import os

def fprintf(fp,format,*args): fp.write(format%args)

def printf(format,*args): sys.stdout.write(format%args)

def load_json(pathIn):
    return json.loads(open(os.path.expanduser(pathIn),"r").read())

def save_json(pathOut,obj):
    open(os.path.expanduser(pathOut),"w").write(json.dumps(obj,indent=2))

def pad(val_str,digits):
    return "0"*(digits-len(val_str))+val_str


def parse_result_rows(file_path):   
    file_path  = os.path.expanduser(file_path)
    lines = open(file_path,"r").read()
    out = []
    for line in lines.split("\n"):
        cols = line.split(",")
        if len(cols) < 4:
            continue
        aid = cols[0]
        lid = cols[1]
        ip = cols[2]
        status = cols[3]
        out.append((aid,lid,ip,status))
    return out
        

host_ips = load_json("./cr7_hosts.json")

hid_file_map = {}
hid_map = {}

results = {}

for host in host_ips:
    cid = host["cluster_id"]
    hid = host["id"]
    ip = host["management_ip"]
    cid_pad = pad(str(cid), 2)
    hid_pad = pad(str(hid), 3)
    hid_file_map["hid_%i.txt"%hid] = "%s_%s.txt"%(cid_pad,hid_pad)
    hid_map["hid_%i.txt"%hid]=hid


for (old_file_name, new_file_name) in hid_file_map.iteritems():
    hid = hid_map[old_file_name]
    old_path = os.path.join("./results",old_file_name)
    new_path = os.path.join("./results",new_file_name)
    printf("hid = %s\n", hid)
    printf("%s -> %s\n", old_path, new_path)
    try:
        os.rename(old_path, new_path)
    except:
        pass
    rows = parse_result_rows(new_path)
    results[hid] = rows

printf("saving results.json\n")
save_json("./results.json", results)
printf("done\n")

