package org.openstack.atlas.service.domain.service.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.datamodel.CorePersistenceType;
import org.openstack.atlas.datamodel.CoreProtocolType;
import org.openstack.atlas.service.domain.entity.ConnectionThrottle;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.SessionPersistence;
import org.openstack.atlas.service.domain.exception.*;
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
    @Transactional(rollbackFor = {EntityNotFoundException.class, ImmutableEntityException.class, UnprocessableEntityException.class})
    public ConnectionThrottle update(Integer loadBalancerId, ConnectionThrottle connectionThrottle) throws PersistenceServiceException {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getById(loadBalancerId);
        ConnectionThrottle dbConnectionThrottle = dbLoadBalancer.getConnectionThrottle();

        if(dbConnectionThrottle == null) {
            dbConnectionThrottle = connectionThrottle;
            dbConnectionThrottle.setLoadBalancer(dbLoadBalancer);
        }

        setPropertiesForUpdate(connectionThrottle, dbConnectionThrottle);

        loadBalancerRepository.changeStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), CoreLoadBalancerStatus.PENDING_UPDATE, false);
        dbLoadBalancer.setConnectionThrottle(dbConnectionThrottle);
        dbLoadBalancer = loadBalancerRepository.update(dbLoadBalancer);
        return dbLoadBalancer.getConnectionThrottle();
    }

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class})
    public void preDelete(Integer loadBalancerId) throws EntityNotFoundException {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getById(loadBalancerId);
        if (dbLoadBalancer.getConnectionThrottle() == null)
            throw new EntityNotFoundException("Connection throttle not found");
    }

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class})
    public void delete(Integer loadBalancerId) throws EntityNotFoundException {
        connectionThrottleRepository.delete(connectionThrottleRepository.getByLoadBalancerId(loadBalancerId));
    }

    protected void setPropertiesForUpdate(final ConnectionThrottle requestConnectionThrottle, final ConnectionThrottle dbConnectionThrottle) throws BadRequestException {
        if (requestConnectionThrottle.getMaxRequestRate() != null)
            dbConnectionThrottle.setMaxRequestRate(requestConnectionThrottle.getMaxRequestRate());
        else throw new BadRequestException("Must provide a max request rate for the request");

        if (requestConnectionThrottle.getRateInterval() != null)
            dbConnectionThrottle.setRateInterval(requestConnectionThrottle.getRateInterval());
        else throw new BadRequestException("Must provide a rate interval for the request");
    }
}
