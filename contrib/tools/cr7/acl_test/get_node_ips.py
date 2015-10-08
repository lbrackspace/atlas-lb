#!/usr/bin/env python
 
import MySQLdb.cursors
import OpenSSL.crypto
import traceback
import operator
import datetime
import OpenSSL
import MySQLdb
import base64
import json
import sys
import os
 
 
lb_query = """
select loadbalancer.account_id as aid,
       loadbalancer.id as lid,
       loadbalancer.protocol as protocol,
       loadbalancer.status as loadbalancer_status,
       host.cluster_id as cid, 
       node.id as node_id,
       node.ip_address as ip_address,
       node.port as port,
       node.node_condition as node_condition,
       node.status as node_status,
       node.type as node_type
           from loadbalancer, host, node
               where loadbalancer.host_id = host.id and
                     node.loadbalancer_id = loadbalancer.id
           order by aid,lid,ip_address;
"""
 
def printf(format,*args): sys.stdout.write(format%args)
 
def load_json(pathIn):
    return json.loads(open(os.path.expanduser(pathIn),"r").read())
 
def save_json(pathOut,obj):
    open(os.path.expanduser(pathOut),"w").write(json.dumps(obj,indent=2))
 
 
def getDbConnection(**conf):
    con = MySQLdb.connect(**conf)
    return con
 
def getLbs(con):
    cur = con.cursor(MySQLdb.cursors.DictCursor)
    cur.execute(lb_query)
    lbs = cur.fetchall()
    return lbs
    
conf = load_json("~/lbaas_dbs.json")
dc="ord"
con = getDbConnection(**conf['db'][dc])
lbs = getLbs(con)
save_json("./ips.json", lbs)


n_nodes_to_scan = 0
for node in lbs:
    if node['cid'] in [1,2,3,4,8,9,10,11,12]:
        n_nodes_to_scan += 1

printf("need to scan %i nodes\n", n_nodes_to_scan)
        
