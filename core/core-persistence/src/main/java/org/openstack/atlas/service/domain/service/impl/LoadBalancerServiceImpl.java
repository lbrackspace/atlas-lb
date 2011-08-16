package org.openstack.atlas.service.domain.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip6;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.service.domain.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class LoadBalancerServiceImpl implements LoadBalancerService {
    private final Log LOG = LogFactory.getLog(LoadBalancerServiceImpl.class);

    @Autowired
    private HostService hostService;

    @Autowired
    private VirtualIpService virtualIpService;

    @Autowired
    private LoadBalancerRepository loadBalancerRepository;

    @Autowired
    private VirtualIpRepository virtualIpRepository;

    @Override
    @Transactional
    public LoadBalancer create(LoadBalancer loadBalancer) throws PersistenceServiceException {
        Validator.verifyTCPProtocolandPort(loadBalancer);
        Validator.verifyProtocolAndHealthMonitorType(loadBalancer);

        LoadBalancerDefaultBuilder.addDefaultValues(loadBalancer);

        loadBalancer.setHost(hostService.getDefaultActiveHost());
        loadBalancer = virtualIpService.assignVIpsToLoadBalancer(loadBalancer);

        LoadBalancer dbLoadBalancer = loadBalancerRepository.create(loadBalancer);
        dbLoadBalancer.setUserName(loadBalancer.getUserName());
        joinIpv6OnLoadBalancer(dbLoadBalancer);
        return dbLoadBalancer;
    }

    @Override
    public LoadBalancer get(Integer id) throws EntityNotFoundException {
        return loadBalancerRepository.getById(id);
    }

    @Override
    @Transactional
    public LoadBalancer update(LoadBalancer lb) throws Exception {
        return loadBalancerRepository.update(lb);
    }

    @Override
    @Transactional
    public LoadBalancer get(Integer id, Integer accountId) throws EntityNotFoundException {
        return loadBalancerRepository.getByIdAndAccountId(id, accountId);
    }

    @Transactional
    private void joinIpv6OnLoadBalancer(LoadBalancer lb) {
        Set<LoadBalancerJoinVip6> loadBalancerJoinVip6SetConfig = lb.getLoadBalancerJoinVip6Set();
        lb.setLoadBalancerJoinVip6Set(null);
        Set<LoadBalancerJoinVip6> newLbVip6Setconfig = new HashSet<LoadBalancerJoinVip6>();
        lb.setLoadBalancerJoinVip6Set(newLbVip6Setconfig);
        for (LoadBalancerJoinVip6 jv6 : loadBalancerJoinVip6SetConfig) {
            LoadBalancerJoinVip6 jv = new LoadBalancerJoinVip6(lb.getPort(), lb, jv6.getVirtualIp());
            virtualIpRepository.persist(jv);
        }
    }
}

