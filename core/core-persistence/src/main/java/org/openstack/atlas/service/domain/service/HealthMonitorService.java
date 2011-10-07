package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.entity.HealthMonitor;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;

public interface HealthMonitorService {
    HealthMonitor update(Integer loadBalancerId, HealthMonitor healthMonitor) throws PersistenceServiceException;

    void preDelete(Integer loadBalancerId) throws PersistenceServiceException;

    void delete(Integer loadBalancerId) throws PersistenceServiceException;
}
