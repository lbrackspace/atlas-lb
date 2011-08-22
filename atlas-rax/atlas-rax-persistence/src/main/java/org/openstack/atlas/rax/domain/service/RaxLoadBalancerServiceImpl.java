package org.openstack.atlas.rax.domain.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.rax.domain.repository.RaxLoadBalancerRepository;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;
import org.openstack.atlas.service.domain.service.*;
import org.openstack.atlas.service.domain.service.impl.LoadBalancerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class RaxLoadBalancerServiceImpl extends LoadBalancerServiceImpl {
    private final Log LOG = LogFactory.getLog(RaxLoadBalancerServiceImpl.class);

    @Autowired
    private AccountLimitService accountLimitService;

    @Autowired
    private BlacklistService blacklistService;

    @Autowired
    private HostService hostService;

    @Autowired
    private RaxLoadBalancerRepository raxLoadBalancerRepository;

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

        LoadBalancer dbLoadBalancer = raxLoadBalancerRepository.create(loadBalancer);
        dbLoadBalancer.setUserName(loadBalancer.getUserName());
        return dbLoadBalancer;
    }
}
