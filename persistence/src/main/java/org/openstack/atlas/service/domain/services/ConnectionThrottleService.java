package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.ConnectionLimit;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.*;

public interface ConnectionThrottleService {

    public ConnectionLimit get(Integer accountId, Integer lbId) throws EntityNotFoundException, DeletedStatusException;

    public void update(LoadBalancer queueLb) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;

    public void prepareForDeletion(LoadBalancer requestLb) throws EntityNotFoundException, UnprocessableEntityException, ImmutableEntityException;

    public void delete(LoadBalancer requestLb) throws EntityNotFoundException;

}
