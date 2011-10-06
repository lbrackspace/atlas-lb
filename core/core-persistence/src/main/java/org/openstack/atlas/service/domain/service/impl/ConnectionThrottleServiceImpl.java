package org.openstack.atlas.service.domain.service.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.datamodel.CorePersistenceType;
import org.openstack.atlas.datamodel.CoreProtocolType;
import org.openstack.atlas.service.domain.entity.ConnectionThrottle;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.SessionPersistence;
import org.openstack.atlas.service.domain.exception.BadRequestException;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;
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
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getById(loadBalancerId);
        ConnectionThrottle dbConnectionThrottle = dbLoadBalancer.getConnectionThrottle();
        ConnectionThrottle connectionThrottleToUpdate = dbConnectionThrottle == null ? connectionThrottle : dbConnectionThrottle;
        connectionThrottleToUpdate.setLoadBalancer(dbLoadBalancer); // Needs to be set for hibernate

        loadBalancerRepository.changeStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), CoreLoadBalancerStatus.PENDING_UPDATE, false);
        setPropertiesForUpdate(connectionThrottle, dbLoadBalancer.getConnectionThrottle(), connectionThrottleToUpdate);
        dbLoadBalancer.setConnectionThrottle(connectionThrottleToUpdate);
        dbLoadBalancer = loadBalancerRepository.update(dbLoadBalancer);
        return dbLoadBalancer.getConnectionThrottle();
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

    protected void setPropertiesForUpdate(final ConnectionThrottle requestThrottle, final ConnectionThrottle dbThrottle, ConnectionThrottle throttleToUpdate) throws BadRequestException {
        if (requestThrottle.getMaxRequestRate() != null) throttleToUpdate.setMaxRequestRate(requestThrottle.getMaxRequestRate());
        else if (dbThrottle != null) throttleToUpdate.setMaxRequestRate(dbThrottle.getMaxRequestRate());
        else throw new BadRequestException("Must provide a max request rate for the request");
        
        if (requestThrottle.getRateInterval() != null) throttleToUpdate.setRateInterval(requestThrottle.getRateInterval());
        else if (dbThrottle != null) throttleToUpdate.setRateInterval(dbThrottle.getRateInterval());
        else throw new BadRequestException("Must provide a rate interval for the request");
    }
}
