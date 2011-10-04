package org.openstack.atlas.service.domain.repository;

import org.openstack.atlas.service.domain.entity.SessionPersistence;
import org.openstack.atlas.service.domain.exception.DeletedStatusException;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;

public interface SessionPersistenceRepository {
    SessionPersistence getSessionPersistenceByLoadBalancerId(Integer loadBalancerId) throws EntityNotFoundException;
}
