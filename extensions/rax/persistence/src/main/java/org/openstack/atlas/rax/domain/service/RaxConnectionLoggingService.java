package org.openstack.atlas.rax.domain.service;

import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.ImmutableEntityException;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;

public interface RaxConnectionLoggingService {
    void update(LoadBalancer loadBalancer) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException;
}
