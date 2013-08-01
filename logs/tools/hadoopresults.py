#!/usr/bin/env python

import MySQLdb.cursors
import MySQLdb
import string
import json
import sys
import os

hadoop_query = """
select * from state 
              where jobname !="LOG_FILE_CF_UPLOAD" 
              and end_time is null;
"""

def max_collengths(rows):
    max_length = {}
    for r in rows:
        for (k,v) in r.iteritems():
            if not max_length.has_key(k):
                max_length[k] = 0
            if max_length[k] < len(str(v)):
                max_length[k] = len(str(v))
    return max_length

def lpad(str_val,padlength,padchar=" "):
    return "%s%s%s%s"%(padchar,str_val,padchar*(padlength-len(str_val)),padchar)

def format_rows(rows,keys=None):
    col_length = max_collengths(rows)
    if keys == None:
        keys = sorted(col_length.keys())
    border = "+" + string.join(["-"*(col_length[k]+2) for k in keys],"+") + "+\n"
    out  = ""
    out += border
    out += "|" + string.join([lpad(str(k),col_length[k]," ") for k in keys],"|") + "|\n"
    out += border
    for r in rows:
        out += "|" + string.join([lpad(str(r[k]),col_length[k]," ") for k in keys],"|") + "|\n"
    out += border
    return out


def printf(format,*args): sys.stdout.write(format%args)

def usage(prog,conf):
    printf("usage is %s <dataCenter>\n",prog)
    printf("\n")
    printf("Scans the database at the specified data center\n")
    printf("to see if hadoop jobs got stuck\n")
    printf("valid choices for data centers are:\n")
    for dc in conf["db"].iterkeys():
        printf("    %s\n",dc)

def load_json(pathIn):
    return json.loads(open(os.path.expanduser(pathIn),"r").read())

def save_json(pathOut,obj):
    open(os.path.expanduser(pathOut),"w").write(json.dumps(obj,indent=2))

def main(*args):
    prog = os.path.basename(args[0])
    conf = load_json("~/lbaas_dbs.json")
    if len(args)<2:
        usage(prog,conf)
        sys.exit()
    dc = args[1]
    dbconf = conf["db"][dc]
    conn = MySQLdb.connect(**dbconf)
    curr = conn.cursor(MySQLdb.cursors.DictCursor)
    curr.execute(hadoop_query)
    rows = curr.fetchall()
    print format_rows(rows,keys=["id","state","jobname","inputpath","start_time","end_time"])

if __name__ == "__main__":
    main(*sys.argv)
