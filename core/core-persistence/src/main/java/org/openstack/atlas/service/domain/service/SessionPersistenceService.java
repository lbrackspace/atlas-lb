package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.entity.SessionPersistence;
import org.openstack.atlas.service.domain.exception.*;

public interface SessionPersistenceService {

    SessionPersistence update(Integer loadBalancerId, SessionPersistence sessionPersistence) throws PersistenceServiceException;

    void preDelete(Integer loadBalancerId) throws PersistenceServiceException;

    void delete(Integer loadBalancerId) throws PersistenceServiceException;
}
