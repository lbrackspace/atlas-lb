package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.SessionPersistence;
import org.openstack.atlas.service.domain.exceptions.*;

public interface SessionPersistenceService {

    public SessionPersistence get(Integer accountId, Integer lbId) throws EntityNotFoundException, BadRequestException, DeletedStatusException;

    public void update(LoadBalancer queueLb) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;

    public void delete(LoadBalancer requestLb) throws Exception;
}
