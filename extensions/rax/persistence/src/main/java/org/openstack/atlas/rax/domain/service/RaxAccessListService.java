package org.openstack.atlas.rax.domain.service;

import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.BadRequestException;
import org.openstack.atlas.service.domain.exception.ImmutableEntityException;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;
import org.springframework.stereotype.Service;

@Service
public interface RaxAccessListService {
    public LoadBalancer updateAccessList(LoadBalancer loadBalancer) throws org.openstack.atlas.service.domain.exception.EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;
}
