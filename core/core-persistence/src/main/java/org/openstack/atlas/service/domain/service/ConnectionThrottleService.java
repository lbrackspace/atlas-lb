package org.openstack.atlas.service.domain.service;


import org.openstack.atlas.service.domain.entity.ConnectionThrottle;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;

public interface ConnectionThrottleService {
    ConnectionThrottle update(Integer loadBalancerId, ConnectionThrottle connectionThrottle) throws PersistenceServiceException;

    void preDelete(Integer loadBalancerId) throws PersistenceServiceException;

    void delete(Integer loadBalancerId) throws PersistenceServiceException;
}
