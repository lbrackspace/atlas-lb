#!/usr/bin/env jython

from util import *
setConfig("slice.xml")
begin()

qStr  = "from Alert a where a.loadbalancerId in "
qStr += "(SELECT h.id from Host h where h.cluster.id = :cid)"

cq = CustomQuery(qStr)
cq.addUnquotedParam("cid",1)
#cq.addParam("a.id","=","aid",21)
#cq.setWherePrefix(" and ")
cq.setWherePrefix("")
cq.getQueryString()


lts = qq("from LimitType")
