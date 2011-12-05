package org.openstack.atlas.rax.domain.service;

import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RaxAccessListService {
    LoadBalancer updateAccessList(LoadBalancer loadBalancer) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;

    LoadBalancer markAccessListForDeletion(Integer accountId, Integer loadBalancerId) throws EntityNotFoundException, UnprocessableEntityException, ImmutableEntityException, DeletedStatusException;

    LoadBalancer markNetworkItemsForDeletion(Integer accountId, Integer loadBalancerId, List<Integer> networkItemIds) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException;

    LoadBalancer markNetworkItemForDeletion(Integer accountId, Integer loadBalancerId, Integer networkItemIds) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException;

}
