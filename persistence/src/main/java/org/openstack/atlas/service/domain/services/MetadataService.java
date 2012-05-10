package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadbalancerMeta;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface MetadataService {
    Set<LoadbalancerMeta> createMetadata(Integer accountId, Integer loadBalancerId, Collection<LoadbalancerMeta> loadbalancerMetas) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;

    Set<LoadbalancerMeta> getMetadataByAccountIdLoadBalancerId(Integer accountId, Integer loadBalancerId) throws EntityNotFoundException;

    LoadbalancerMeta getMeta(Integer accountId, Integer loadBalancerId, Integer id) throws EntityNotFoundException;

    void deleteMeta(Integer accountId, Integer loadBalancerId, Integer id) throws EntityNotFoundException;

    void updateMeta(LoadBalancer domainLb) throws EntityNotFoundException;

    List<String> prepareForMetadataDeletion(Integer accountId, Integer loadBalancerId, List<Integer> ids) throws EntityNotFoundException;

    LoadBalancer deleteMetadata(LoadBalancer lb, Collection<Integer> ids) throws EntityNotFoundException;
}
