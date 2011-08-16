package org.openstack.atlas.service.domain.service.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.service.ConnectionThrottleService;
import org.springframework.stereotype.Service;

@Service
public class ConnectionThrottleServiceImpl implements ConnectionThrottleService {
    private final Log LOG = LogFactory.getLog(ConnectionThrottleServiceImpl.class);

}
