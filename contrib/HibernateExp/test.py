#!/usr/bin/env jython
	
import com.zxtm.service.client.CertificateFiles as CertificateFiles
import util
util.setConfig("ndev.json")
from util import *

begin()


lbe = LoadBalancerServiceEvent()
lbe.getId() 
lbe.setTitle("Test title")
lbe.setDetailedMessage("Test Detailed Message")
lbe.setLoadbalancerId(-1)
lbe.setAccountId(-1)
lbe.setCategory(CategoryType.UPDATE)
lbe.setSeverity(EventSeverity.INFO)
lbe.setRelativeUri("Some Uri")
lbe.setCreated(Calendar.getInstance())
lbe.setType(EventType.UPDATE_LOADBALANCER)

