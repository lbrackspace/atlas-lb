package org.openstack.atlas.service.domain.services.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.ConnectionLimit;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.services.ConnectionThrottleService;
import org.openstack.atlas.service.domain.services.LoadBalancerStatusHistoryService;
import org.openstack.atlas.service.domain.services.helpers.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConnectionThrottleServiceImpl extends BaseService implements ConnectionThrottleService {
    private final Log LOG = LogFactory.getLog(ConnectionThrottleServiceImpl.class);

    @Autowired
    private LoadBalancerStatusHistoryService loadBalancerStatusHistoryService;

    @Override
    public ConnectionLimit get(Integer accountId, Integer lbId) throws EntityNotFoundException, DeletedStatusException {
        return loadBalancerRepository.getConnectionLimitsbyAccountIdLoadBalancerId(accountId, lbId);
    }

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, ImmutableEntityException.class, UnprocessableEntityException.class, BadRequestException.class})
    public void update(LoadBalancer queueLb) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException {
        LOG.debug("Entering " + getClass());
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(queueLb.getId(), queueLb.getAccountId());

        ConnectionLimit queueThrottle = queueLb.getConnectionLimit();
        ConnectionLimit dbThrottle = dbLoadBalancer.getConnectionLimit();
        ConnectionLimit newThrottle = new ConnectionLimit();

        if (queueThrottle.getMaxConnectionRate() != null) {
            newThrottle.setMaxConnectionRate(queueThrottle.getMaxConnectionRate());
        } else if (dbThrottle != null) {
            newThrottle.setMaxConnectionRate(dbThrottle.getMaxConnectionRate());
        } else {
            newThrottle.setMaxConnectionRate(null);
        }

        if (queueThrottle.getMinConnections() != null) {
            newThrottle.setMinConnections(queueThrottle.getMinConnections());
        } else if (dbThrottle != null) {
            newThrottle.setMinConnections(dbThrottle.getMinConnections());
        } else {
            newThrottle.setMinConnections(null);
        }

        if (queueThrottle.getMaxConnections() != null) {
            newThrottle.setMaxConnections(queueThrottle.getMaxConnections());
        } else if (dbThrottle != null) {
            newThrottle.setMaxConnections(dbThrottle.getMaxConnections());
        } else {
            newThrottle.setMaxConnections(null);
        }

        if (queueThrottle.getRateInterval() != null) {
            newThrottle.setRateInterval(queueThrottle.getRateInterval());
        } else if (dbThrottle != null) {
            newThrottle.setRateInterval(dbThrottle.getRateInterval());
        } else {
            newThrottle.setRateInterval(null);
        }

        queueLb.setConnectionLimit(newThrottle);

        ConnectionLimit updatedThrottle = queueLb.getConnectionLimit();
        if (dbThrottle == null) {
            if (updatedThrottle.getMaxConnectionRate() == null || updatedThrottle.getMaxConnections() == null ||
                    updatedThrottle.getMinConnections() == null || updatedThrottle.getRateInterval() == null) {
                throw new BadRequestException("Must supply all credentials when creating a connection throttle.");
            }
        }

        LOG.debug("Updating the lb status to pending_update");
        if(!loadBalancerRepository.testAndSetStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE, false)) {
            String message = StringHelper.immutableLoadBalancer(dbLoadBalancer);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        } else {
            //Set status record
            loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE);
        }

        LOG.debug("Updating the loadbalancer and connection dbThrottle in the database...");
        if (dbThrottle != null) {
            loadBalancerRepository.updateConnectionLimit(dbLoadBalancer, queueLb.getConnectionLimit());
        } else {
            loadBalancerRepository.createConnectionLimit(dbLoadBalancer, queueLb.getConnectionLimit());
        }

        LOG.debug("Leaving " + getClass());
    }

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, ImmutableEntityException.class, UnprocessableEntityException.class})
    public void prepareForDeletion(LoadBalancer requestLb) throws EntityNotFoundException, UnprocessableEntityException, ImmutableEntityException {
        LOG.debug("Entering " + getClass());
        LoadBalancer dbLb = loadBalancerRepository.getByIdAndAccountId(requestLb.getId(), requestLb.getAccountId());

        if (dbLb.getConnectionLimit() == null) {
            throw new UnprocessableEntityException("No connection throttle found to delete.");
        }

        if(!loadBalancerRepository.testAndSetStatus(dbLb.getAccountId(), dbLb.getId(), LoadBalancerStatus.PENDING_UPDATE, false)) {
            String message = StringHelper.immutableLoadBalancer(dbLb);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        } else {
            //Set status record
            loadBalancerStatusHistoryService.save(dbLb.getAccountId(), dbLb.getId(), LoadBalancerStatus.PENDING_UPDATE);
        }
    }

    @Override
    @Transactional
    public void delete(LoadBalancer requestLb) throws EntityNotFoundException {
        LoadBalancer dbLb = loadBalancerRepository.getByIdAndAccountId(requestLb.getId(), requestLb.getAccountId());
        loadBalancerRepository.removeConnectionThrottle(dbLb);
    }
}
