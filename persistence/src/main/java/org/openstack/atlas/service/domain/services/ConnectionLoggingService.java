package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;

public interface ConnectionLoggingService {
    public boolean get(Integer accountId, Integer lbId) throws EntityNotFoundException;

    public void update(LoadBalancer queueLb) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException;
}
