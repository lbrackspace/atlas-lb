#!/usr/bin/env jython

from db import *
from db_classes import *

app = getDb()


begin()
p = SslCipherProfile()
p.setName("test")
p.setComments("Field is not null for some reason")
p.setCiphers("a,b,c")
app.save(p)
commit()

begin()
p = qq("from SslCipherProfile where name='test'")[0]
s1 = SslTermination()
s1.setCipher_list("a,d,h")
s1.setSslCipherProfile(p)
app.save(s1)
commit()


