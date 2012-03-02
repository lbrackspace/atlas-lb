#!/usr/bin/env jython
	
import com.zxtm.service.client.CertificateFiles as CertificateFiles
import util
util.setConfig("ndev.json")
from util import *

begin()

q  = "SELECT lbe from LoadBalancerServiceEvent lbe"
q += "     WHERE lbe.author = 'dead2hill' ORDER BY lbe.created ASC"

startTime = isoTocal("2012-02-02T11:35:00")
endTimee  = isoTocal("2012-02-02T11:50:00")

res = qq(q)

bunkQ  = "SELECT lbe FROM LoadBalancerServiceEvent lbe WHERE lbe.author ="
bunkQ += ":author AND lbe.created BETWEEN :startDate AND :endDate"
bunkO += " ORDER BY lbe.created ASC"

