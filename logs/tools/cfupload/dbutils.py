#!/usr/bin/env python

import MySQLdb
import utils

def getrows(cur,result):
  out = []
  col_count = len(cur.description)
  row_count = len(result)
  for i in xrange(0,row_count):
    tmp_hash={}
    for j in xrange(0,col_count):
      tmp_hash[cur.description[j][0].lower()]=result[i][j]
    out.append(tmp_hash)
  return out

def getrow(des,row):
  out = {}
  for i in xrange(0,len(des)):
     out[ des[i][0].lower() ]=row[i]
  return out

def getcols(cur,result):
    out = []
    col_count = len(cur.description)
    for i in xrange(0,col_count):
        out.append(cur.description[i][0].lower())
    return out

def getvals(rows,cols):
    vals = []
    for row in rows:
        tmp = []
        for col in cols:
            tmp.append(row[col])
        vals.append(tmp)
    return vals

def replace(rows,table):
    rows_out = []
    complete_str = ""
    col_str = ""
    vals = []
    vals_str = ""
    cols = rows[0].keys()
    cols.sort()
    for col in cols[:-1]:
        col_str += "%s,"%col
        vals_str += "%s, "
    col_str += "%s"%cols[-1]
    vals_str += "%s"
    complete_str = "replace into %s (%s) values (%s) "%(table,col_str,vals_str)
    for row in rows:
        newrow = []
        for col in cols:
            newrow.append(row[col])
        rows_out.append(tuple(newrow))
    return (complete_str,rows_out)

def getcol_names(cur,table):
  query = "describe %s;"%(table)
  cur.execute(query)
  result = cur.fetchall()
  rows = getrows(cur,result)
  cols = []
  for row in rows:
    cols.append(row['field'].lower())
  return(cols)

def insert(rows,table):
    rows_out = []
    complete_str = ""
    col_str = ""
    vals = []
    vals_str = ""
    cols = rows[0].keys()
    cols.sort()
    for col in cols[:-1]:
        col_str += "%s,"%col
        vals_str += "%s, "
    col_str += "%s"%cols[-1]
    vals_str += "%s"
    complete_str = "insert into %s (%s) values (%s) "%(table,col_str,vals_str)
    for row in rows:
        newrow = []
        for col in cols:
            newrow.append(row[col])
        rows_out.append(tuple(newrow))
    return (complete_str,rows_out)

def updaterow(row_in,table,where_cols):
    set_cols = list( set(row_in.keys()) - set(where_cols) )
    set_vals = [ row_in[k] for k in set_cols]
    where_vals = [ row_in[k] for k in where_cols]
    update_str = "update %s set "%table

    #build set clause
    for col in set_cols[:-1]:
        update_str += "%s = "%col + "%s, "
    col = set_cols[-1]
    update_str += "%s = "%col + "%s"


    #build set clause
    update_str += " where "
    for col in where_cols[:-1]:
        update_str += "%s = "%col + "%s and  "
    col = where_cols[-1]
    update_str += "%s = "%col + "%s"
    vals = tuple(set_vals + where_vals)
    return (update_str,vals)

def getConCur(**cred):
    con = MySQLdb.connect(**cred)
    cur = con.cursor()
    return (con,cur)

