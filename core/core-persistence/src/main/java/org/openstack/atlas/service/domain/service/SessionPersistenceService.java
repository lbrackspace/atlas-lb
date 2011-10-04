package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.entity.SessionPersistence;
import org.openstack.atlas.service.domain.exception.*;

public interface SessionPersistenceService {

    public void update(Integer loadBalancerId, SessionPersistence sessionPersistence) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException;

    public SessionPersistence get(Integer loadBalancerId) throws EntityNotFoundException;

    public void delete(Integer loadBalancerId) throws Exception;
}
