package org.openstack.atlas.service.domain.repository;

import org.openstack.atlas.service.domain.entity.HealthMonitor;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;

public interface HealthMonitorRepository {
    HealthMonitor getByLoadBalancerId(Integer loadBalancerId) throws EntityNotFoundException;

    void delete(HealthMonitor healthMonitor) throws EntityNotFoundException;
}
