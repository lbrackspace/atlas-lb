package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.entity.SessionPersistence;
import org.openstack.atlas.service.domain.exception.*;

public interface SessionPersistenceService {

    public SessionPersistence update(Integer loadBalancerId, SessionPersistence sessionPersistence) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;

    public void delete(Integer loadBalancerId) throws EntityNotFoundException;
}
