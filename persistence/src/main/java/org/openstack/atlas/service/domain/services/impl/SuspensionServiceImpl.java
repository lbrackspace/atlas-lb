package org.openstack.atlas.service.domain.services.impl;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.services.LoadBalancerStatusHistoryService;
import org.openstack.atlas.service.domain.services.SuspensionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SuspensionServiceImpl extends BaseService implements SuspensionService {

    @Autowired
    private LoadBalancerStatusHistoryService loadBalancerStatusHistoryService;

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

//        if (!isActiveLoadBalancer(dbLoadBalancer, false)) {
//            LOG.debug(String.format("Loadbalancer %d is currently immutable. Canceling request...", queueLb.getId()));
//            throw new ImmutableEntityException(String.format("Loadbalancer %d is currently immutable. Canceling request...", queueLb.getId()));
//        }

        if(!loadBalancerRepository.testAndSetStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE, false)) {
            String message = String.format("Load balancer %d is considered immutable and cannot process request", queueLb.getId());
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        } else {
            //Set status record
            loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE);
        }

        if (dbLoadBalancer.getSuspension() != null) {
            LOG.debug(String.format("Suspension already exists for loadbalancer %d.", queueLb.getId()));
            throw new UnprocessableEntityException(String.format("A suspension already exists."));
        }

        LOG.debug("Updating the lb status to pending_update");
//        dbLoadBalancer.setStatus(LoadBalancerStatus.PENDING_UPDATE);

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
//        dbLoadBalancer.setStatus(LoadBalancerStatus.PENDING_UPDATE);
        loadBalancerRepository.setStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE);

        //Set status record
        loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE);

    }

}
