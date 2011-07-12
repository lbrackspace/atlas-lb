package org.openstack.atlas.service.domain.services.impl;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.services.SuspensionService;
import org.springframework.transaction.annotation.Transactional;

public class SuspensionServiceImpl extends BaseService implements SuspensionService {

    @Override
    @Transactional
    public void createSuspension(LoadBalancer queueLb) throws Exception {
        LOG.debug("Entering " + getClass());

        LoadBalancer dbLoadBalancer;

        try {
            dbLoadBalancer = loadBalancerRepository.getById(queueLb.getId());
        } catch (EntityNotFoundException enfe) {
            LOG.warn("EntityNotFoundException thrown. Sending error response to client...");
            throw new EntityNotFoundException(String.format("Cannot find loadbalancer with id #%d", queueLb.getId()));
        }

        if (!isActiveLoadBalancer(dbLoadBalancer, false)) {
            LOG.debug(String.format("Loadbalancer %d is currently immutable. Canceling request...", queueLb.getId()));
            throw new ImmutableEntityException(String.format("Loadbalancer %d is currently immutable. Canceling request...", queueLb.getId()));
        }

        if (dbLoadBalancer.getSuspension() != null) {
            LOG.debug(String.format("Suspension already exists for loadbalancer %d.", queueLb.getId()));
            throw new UnprocessableEntityException(String.format("A suspension already exists."));
        }

        LOG.debug("Updating the lb status to pending_update");
        dbLoadBalancer.setStatus(LoadBalancerStatus.PENDING_UPDATE);

        LOG.debug("Leaving " + getClass());
    }

    @Override
    @Transactional
    public void deleteSuspension(LoadBalancer queueLb) throws Exception {
        LOG.debug("Entering " + getClass());
        LoadBalancer dbLoadBalancer;

        try {
            dbLoadBalancer = loadBalancerRepository.getById(queueLb.getId());
        } catch (EntityNotFoundException enfe) {
            LOG.warn("EntityNotFoundException thrown. Sending error response to client...");
            throw new EntityNotFoundException(String.format("Loadbalancer with id #%d not found for account #%d", queueLb.getId(), queueLb.getAccountId()));
        }

        if (dbLoadBalancer.getSuspension() == null) {
            throw new EntityNotFoundException(String.format("No Suspension found to remove."));
        }

        LOG.debug("Updating the lb status to pending_update");
        dbLoadBalancer.setStatus(LoadBalancerStatus.PENDING_UPDATE);
    }

}
