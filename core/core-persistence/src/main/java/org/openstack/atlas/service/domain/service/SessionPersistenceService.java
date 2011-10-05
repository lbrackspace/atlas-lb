package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.entity.SessionPersistence;
import org.openstack.atlas.service.domain.exception.*;

public interface SessionPersistenceService {

    SessionPersistence update(Integer loadBalancerId, SessionPersistence sessionPersistence) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;

    void preDelete(Integer loadBalancerId) throws EntityNotFoundException;

    void delete(Integer loadBalancerId) throws EntityNotFoundException;
}
