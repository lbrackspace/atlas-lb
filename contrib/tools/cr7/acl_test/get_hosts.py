#!/usr/bin/env python

import MySQLdb.cursors
import MySQLdb
import string
import json
import sys
import os

def fprintf(fp,format,*args): fp.write(format%args)

def printf(format,*args): sys.stdout.write(format%args)

def load_json(pathIn):
    return json.loads(open(os.path.expanduser(pathIn),"r").read())

def save_json(pathOut,obj):
    open(os.path.expanduser(pathOut),"w").write(json.dumps(obj,indent=2))


def getDbConnection(**conf):
    con = MySQLdb.connect(**conf)
    return con

def getHostIps(con):
    cur = con.cursor(MySQLdb.cursors.DictCursor)
    cur.execute(lb_query)
    ips = cur.fetchall()
    return ips

lb_query = """
select id,management_ip,cluster_id 
    from host where cluster_id in (1,2,3,4,8,9,10,11,12);
"""



if __name__ == "__main__":
    conf = load_json("~/lbaas_dbs.json")
    dc = "ord"
    con = getDbConnection(**conf['db'][dc])
    host_ips = getHostIps(con)
    save_json("./cr7_hosts.json", host_ips)
    host_fp = open("./hosts.txt","w")
    host_map_fp = open("./host_map.txt","w")
    for h in host_ips:
        hid = h['id']
        cid = h['cluster_id']
        ip = h['management_ip']
        fprintf(host_fp,"%s\n",ip)
        fprintf(host_map_fp, "hid_%i.txt,%s ", hid, ip)
        printf("%i, %i, %s\n", hid,cid,ip)
    host_fp.close()
    host_map_fp.close()
