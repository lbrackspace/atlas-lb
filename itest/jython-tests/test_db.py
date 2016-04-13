
from db import *
from db_classes import *

import org.openstack.atlas.util.ip.IPUtils as IPUtils

begin()
t=qq("select s from SslTermination s")
t[0].setTls10Enabled(True)
app.save(t[0])
commit()

begin()
query = """select s.loadbalancer.id, 
           s.loadbalancer.accountId,
           s.id,
           s.privatekey,
           s.certificate,
           s.intermediateCertificate
           from SslTermination s
app = getDb("./dfw.json")




query = """
select n.loadbalancer.id, n.ipAddress,  n.port, n.id, n.status  from Node n
    where n.loadbalancer.status='ACTIVE' and n.condition='ENABLED'
    order by n.loadbalancer.id, n.id
"""

begin()
nodes = qq(query)

ipv4 = []
ipv6 = []
hosts = []
wtf = []
begin()
nodes = qq(query)
commit()

for n in nodes:
    lid = n[0]
    ipAddress = n[1]
    port = n[2]
    nid = n[3]
    ipType = IPUtils.getIPType(ipAddress)
    if ipType == IPUtils.IPv4:
        ipv4.append(n)
    elif ipType == IPUtils.IPv6:
        ipv6.append(n)
    elif ipType == IPUtils.HOST_NAME:
        hosts.append(n)
    else:
        wtf.append(n)
