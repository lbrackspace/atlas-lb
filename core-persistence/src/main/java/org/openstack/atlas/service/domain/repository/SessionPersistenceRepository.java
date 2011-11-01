package org.openstack.atlas.service.domain.repository;

import org.openstack.atlas.service.domain.entity.SessionPersistence;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;

public interface SessionPersistenceRepository {
    SessionPersistence getByLoadBalancerId(Integer loadBalancerId) throws EntityNotFoundException;

    void delete(SessionPersistence sessionPersistence) throws EntityNotFoundException;
}
