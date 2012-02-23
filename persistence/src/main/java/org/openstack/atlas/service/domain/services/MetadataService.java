package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.Meta;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;

import java.util.Collection;
import java.util.Set;

public interface MetadataService {
    Set<Meta> createMetadata(Integer accountId, Integer loadBalancerId, Collection<Meta> metas) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;

    Set<Meta> getMetadataByAccountIdLoadBalancerId(Integer accountId, Integer loadBalancerId) throws EntityNotFoundException;
}
