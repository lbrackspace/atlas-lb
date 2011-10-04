package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.SessionPersistence;
import org.openstack.atlas.service.domain.exception.*;

public interface SessionPersistenceService {

    public SessionPersistence get(Integer accountId, Integer lbId) throws EntityNotFoundException, BadRequestException, DeletedStatusException;

    public void update(LoadBalancer queueLb) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException;

    public void delete(LoadBalancer requestLb) throws Exception;
}
