package org.openstack.atlas.service.domain.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoadBalancerServiceImpl implements LoadBalancerService {
    private final Log LOG = LogFactory.getLog(LoadBalancerServiceImpl.class);

    @Autowired
    private AccountLimitService accountLimitService;

    @Autowired
    private BlacklistService blacklistService;

    @Autowired
    private HostService hostService;

    @Autowired
    private LoadBalancerRepository loadBalancerRepository;

    @Autowired
    private VirtualIpService virtualIpService;

    @Override
    @Transactional
    public LoadBalancer create(LoadBalancer loadBalancer) throws PersistenceServiceException {
        Validator.verifyTCPProtocolandPort(loadBalancer);
        Validator.verifyProtocolAndHealthMonitorType(loadBalancer);

        accountLimitService.verifyLoadBalancerLimit(loadBalancer.getAccountId());
        blacklistService.verifyNoBlacklistNodes(loadBalancer.getNodes());

        LoadBalancerDefaultBuilder.addDefaultValues(loadBalancer);

        loadBalancer.setHost(hostService.getDefaultActiveHost());
        loadBalancer = virtualIpService.assignVIpsToLoadBalancer(loadBalancer);

        LoadBalancer dbLoadBalancer = loadBalancerRepository.create(loadBalancer);
        dbLoadBalancer.setUserName(loadBalancer.getUserName());
        return dbLoadBalancer;
    }
}

