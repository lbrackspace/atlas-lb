package org.openstack.atlas.service.domain.service.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.service.HealthMonitorService;
import org.springframework.stereotype.Service;

@Service
public class HealthMonitorServiceImpl implements HealthMonitorService {
    private final Log LOG = LogFactory.getLog(HealthMonitorServiceImpl.class);
}
