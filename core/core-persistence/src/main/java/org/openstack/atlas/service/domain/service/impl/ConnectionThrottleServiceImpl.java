package org.openstack.atlas.service.domain.service.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entity.ConnectionThrottle;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;
import org.openstack.atlas.service.domain.repository.ConnectionThrottleRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.ConnectionThrottleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConnectionThrottleServiceImpl implements ConnectionThrottleService {
    private final Log LOG = LogFactory.getLog(ConnectionThrottleServiceImpl.class);

    @Autowired
    protected LoadBalancerRepository loadBalancerRepository;
    @Autowired
    protected ConnectionThrottleRepository connectionThrottleRepository;

    @Override
    @Transactional
    public ConnectionThrottle update(Integer loadBalancerId, ConnectionThrottle connectionThrottle) throws PersistenceServiceException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class})
    public void preDelete(Integer loadBalancerId) throws EntityNotFoundException {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getById(loadBalancerId);
        if (dbLoadBalancer.getConnectionThrottle() == null) throw new EntityNotFoundException("Connection throttle not found");
    }

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class})
    public void delete(Integer loadBalancerId) throws EntityNotFoundException {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getById(loadBalancerId);
        connectionThrottleRepository.delete(dbLoadBalancer.getConnectionThrottle());
    }
}
