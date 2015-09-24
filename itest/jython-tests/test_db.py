
from db import *
from db_classes import *

app = getDb()

begin()
query = """select s.loadbalancer.id, 
           s.loadbalancer.accountId,
           s.id,
           s.privatekey,
           s.certificate,
           s.intermediateCertificate
           from SslTermination s
"""

rows = qq(query)
r = rows[0]
for n in r: print n

