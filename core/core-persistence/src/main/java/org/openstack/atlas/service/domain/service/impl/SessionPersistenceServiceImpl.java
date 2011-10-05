package org.openstack.atlas.service.domain.service.impl;

import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.SessionPersistence;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.ImmutableEntityException;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.SessionPersistenceRepository;
import org.openstack.atlas.service.domain.service.SessionPersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SessionPersistenceServiceImpl implements SessionPersistenceService {

    @Autowired
    protected SessionPersistenceRepository sessionPersistenceRepository;
    @Autowired
    protected LoadBalancerRepository loadBalancerRepository;


    @Override
    public void update(Integer loadBalancerId, SessionPersistence sessionPersistence) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SessionPersistence get(Integer loadBalancerId) throws EntityNotFoundException {
        return sessionPersistenceRepository.getSessionPersistenceByLoadBalancerId(loadBalancerId);
    }

    @Override
    public void delete(Integer loadBalancerId) throws EntityNotFoundException {
        LoadBalancer loadBalancer = loadBalancerRepository.getById(loadBalancerId);
        sessionPersistenceRepository.delete(loadBalancer.getSessionPersistence());
    }
}
