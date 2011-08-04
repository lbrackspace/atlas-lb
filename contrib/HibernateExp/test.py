#!/usr/bin/env jython

from util import *
setConfig("slice.xml")

begin()
lb = qq("FROM LoadBalancer lb where lb.id=316")[0]


lb.getUserPages().getErrorpage()

begin()
ep = qq("FROM UserPages up where id=1")[0]

count = 1024
goaway = "<html>%s</html>"%("Test<br/>\n"*count)
ep[0].setErrorpage(goaway)
saveList(ep)

