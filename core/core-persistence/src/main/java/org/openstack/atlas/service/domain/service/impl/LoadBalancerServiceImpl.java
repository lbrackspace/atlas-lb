package org.openstack.atlas.service.domain.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.common.*;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancerProtocol;
import org.openstack.atlas.service.domain.entity.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entity.SessionPersistence;
import org.openstack.atlas.service.domain.exception.*;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class LoadBalancerServiceImpl implements LoadBalancerService {
    protected final Log LOG = LogFactory.getLog(LoadBalancerServiceImpl.class);

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
    public final LoadBalancer create(final LoadBalancer loadBalancer) throws PersistenceServiceException {
        validateCreate(loadBalancer);
        addDefaultValuesForCreate(loadBalancer);
        LoadBalancer dbLoadBalancer = loadBalancerRepository.create(loadBalancer);
        dbLoadBalancer.setUserName(loadBalancer.getUserName());
        return dbLoadBalancer;
    }

    @Override
    @Transactional
    public LoadBalancer update(final LoadBalancer loadBalancer) throws PersistenceServiceException {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(loadBalancer.getId(), loadBalancer.getAccountId());

        loadBalancerRepository.changeStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE);

        setName(loadBalancer, dbLoadBalancer);
        setAlgorithm(loadBalancer, dbLoadBalancer);

        dbLoadBalancer = loadBalancerRepository.update(dbLoadBalancer);
        dbLoadBalancer.setUserName(loadBalancer.getUserName());

        return dbLoadBalancer;
    }

    @Override
    @Transactional
    public void preDelete(final LoadBalancer lb) throws PersistenceServiceException {
        List<Integer> loadBalancerIds = new ArrayList<Integer>();
        loadBalancerIds.add(lb.getId());
        preDelete(lb.getAccountId(), loadBalancerIds);
    }

    @Override
    @Transactional
    public void preDelete(final Integer accountId, final List<Integer> loadBalancerIds) throws PersistenceServiceException {
        validateDelete(accountId, loadBalancerIds);
        for(int lbId : loadBalancerIds) {
            loadBalancerRepository.changeStatus(lbId, accountId, LoadBalancerStatus.PENDING_DELETE);
        }
    }

    @Override
    @Transactional
    public void delete(final LoadBalancer lb) throws PersistenceServiceException {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(lb.getId(), lb.getAccountId());
        dbLoadBalancer.setStatus(LoadBalancerStatus.DELETED);
        dbLoadBalancer = loadBalancerRepository.update(dbLoadBalancer);
        virtualIpService.removeAllVipsFromLoadBalancer(dbLoadBalancer);
    }

    protected void setAlgorithm(final LoadBalancer loadBalancer, final LoadBalancer dbLoadBalancer) {
        if (loadBalancer.getAlgorithm() != null && !loadBalancer.getAlgorithm().equals(dbLoadBalancer.getAlgorithm())) {
            LOG.debug("Updating loadbalancer algorithm to " + loadBalancer.getAlgorithm());
            dbLoadBalancer.setAlgorithm(loadBalancer.getAlgorithm());
        }
    }

    protected void setName(final LoadBalancer loadBalancer, final LoadBalancer dbLoadBalancer) {
        if (loadBalancer.getName() != null && !loadBalancer.getName().equals(dbLoadBalancer.getName())) {
            LOG.debug("Updating loadbalancer name to " + loadBalancer.getName());
            dbLoadBalancer.setName(loadBalancer.getName());
        }
    }

    protected void validateDelete(final Integer accountId, final List<Integer> loadBalancerIds) throws BadRequestException {
        List<Integer> badLbIds = new ArrayList<Integer>();
        List<Integer> badLbStatusIds = new ArrayList<Integer>();
        for (int loadBalancerId : loadBalancerIds) {
            try {
                LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(loadBalancerId, accountId);
                if(!dbLoadBalancer.getStatus().equals(LoadBalancerStatus.ACTIVE)) {
                    LOG.warn(StringHelper.immutableLoadBalancer(dbLoadBalancer));
                    badLbStatusIds.add(loadBalancerId);
                }
            } catch (EntityNotFoundException e) {
                badLbIds.add(loadBalancerId);
            }
        }
        if (!badLbIds.isEmpty()) {
            throw new BadRequestException(ErrorMessages.LBS_NOT_FOUND.getMessage(StringUtilities.DelimitString(badLbIds, ",")));
        }
        if (!badLbStatusIds.isEmpty()) {
            throw new BadRequestException(ErrorMessages.LBS_IMMUTABLE.getMessage(StringUtilities.DelimitString(badLbStatusIds, ",")));
        }
    }

    protected void validateCreate(final LoadBalancer loadBalancer) throws BadRequestException, EntityNotFoundException, LimitReachedException {
        Validator.verifyTCPProtocolandPort(loadBalancer);
        Validator.verifyProtocolAndHealthMonitorType(loadBalancer);
        accountLimitService.verifyLoadBalancerLimit(loadBalancer.getAccountId());
        blacklistService.verifyNoBlacklistNodes(loadBalancer.getNodes());
    }

    protected void addDefaultValuesForCreate(final LoadBalancer loadBalancer) throws PersistenceServiceException {
        LoadBalancerDefaultBuilder.addDefaultValues(loadBalancer);
        loadBalancer.setHost(hostService.getDefaultActiveHost());
        virtualIpService.assignVIpsToLoadBalancer(loadBalancer);
    }
}

