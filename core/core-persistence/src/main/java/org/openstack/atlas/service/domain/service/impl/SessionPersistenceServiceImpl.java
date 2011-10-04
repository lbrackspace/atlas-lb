package org.openstack.atlas.service.domain.service.impl;

import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.SessionPersistence;
import org.openstack.atlas.service.domain.exception.*;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.SessionPersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SessionPersistenceServiceImpl implements SessionPersistenceService {

    @Autowired
    protected LoadBalancerRepository loadBalancerRepository;

    @Override
    public SessionPersistence get(Integer accountId, Integer lbId) throws EntityNotFoundException, BadRequestException, DeletedStatusException {
        return loadBalancerRepository.getSessionPersistenceByAccountIdLoadBalancerId(accountId, lbId);
    }

    @Override
    public void update(LoadBalancer queueLb) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void delete(LoadBalancer requestLb) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
