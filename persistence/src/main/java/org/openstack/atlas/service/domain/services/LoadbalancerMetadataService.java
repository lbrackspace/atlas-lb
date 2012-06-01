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

public interface LoadbalancerMetadataService {
    Set<LoadbalancerMeta> createLoadbalancerMetadata(Integer accountId, Integer loadBalancerId, Collection<LoadbalancerMeta> loadbalancerMetas) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;

    Set<LoadbalancerMeta> getLoadbalancerMetadataByAccountIdLoadBalancerId(Integer accountId, Integer loadBalancerId) throws EntityNotFoundException;

    LoadbalancerMeta getLoadbalancerMeta(Integer accountId, Integer loadBalancerId, Integer id) throws EntityNotFoundException;

    void deleteLoadbalancerMeta(Integer accountId, Integer loadBalancerId, Integer id) throws EntityNotFoundException;

    void updateLoadbalancerMeta(LoadBalancer domainLb) throws EntityNotFoundException;

    List<String> prepareForLoadbalancerMetadataDeletion(Integer accountId, Integer loadBalancerId, List<Integer> ids) throws EntityNotFoundException;

    LoadBalancer deleteMetadata(LoadBalancer lb, Collection<Integer> ids) throws EntityNotFoundException;
}
