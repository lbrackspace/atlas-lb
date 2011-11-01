package org.openstack.atlas.service.domain.repository;

import org.openstack.atlas.service.domain.entity.ConnectionThrottle;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;

public interface ConnectionThrottleRepository {
    ConnectionThrottle getByLoadBalancerId(Integer loadBalancerId) throws EntityNotFoundException;

    void delete(ConnectionThrottle connectionThrottle) throws EntityNotFoundException;
}
