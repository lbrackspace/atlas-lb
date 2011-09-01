package org.openstack.atlas.service.domain.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.BadRequestException;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.LimitReachedException;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;

@Service
public class LoadBalancerServiceImpl implements LoadBalancerService {
    private final Log LOG = LogFactory.getLog(LoadBalancerServiceImpl.class);

    @Autowired
    protected AccountLimitService accountLimitService;

    @Autowired
    protected BlacklistService blacklistService;

    @Autowired
    protected HostService hostService;

    @Autowired
    protected LoadBalancerRepository loadBalancerRepository;

    @Autowired
    protected VirtualIpService virtualIpService;

    @Override
    @Transactional
    public final LoadBalancer create(LoadBalancer loadBalancer) throws PersistenceServiceException {
        validate(loadBalancer);
        loadBalancer = addDefaultValues(loadBalancer);
        LoadBalancer dbLoadBalancer = loadBalancerRepository.create(loadBalancer);
        dbLoadBalancer.setUserName(loadBalancer.getUserName());
        return dbLoadBalancer;
    }

    protected void validate(LoadBalancer loadBalancer) throws BadRequestException, EntityNotFoundException, LimitReachedException {
        Validator.verifyTCPProtocolandPort(loadBalancer);
        Validator.verifyProtocolAndHealthMonitorType(loadBalancer);
        accountLimitService.verifyLoadBalancerLimit(loadBalancer.getAccountId());
        blacklistService.verifyNoBlacklistNodes(loadBalancer.getNodes());
    }

    protected LoadBalancer addDefaultValues(LoadBalancer loadBalancer) throws PersistenceServiceException {
        LoadBalancerDefaultBuilder.addDefaultValues(loadBalancer);
        loadBalancer.setHost(hostService.getDefaultActiveHost());
        loadBalancer = virtualIpService.assignVIpsToLoadBalancer(loadBalancer);
        return loadBalancer;
    }
}

