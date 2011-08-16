package org.openstack.atlas.service.domain.services;


import org.openstack.atlas.service.domain.entities.AccessList;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.DeletedStatusException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;

import java.util.List;
import java.util.Set;

public interface AccessListService {

    List<AccessList> getAccessListByAccountIdLoadBalancerId(int accountId, int loadbalancerId,
                                                                   Integer... p) throws EntityNotFoundException, DeletedStatusException;

    public LoadBalancer markForDeletionNetworkItems(LoadBalancer returnLB, List<Integer> networkItemIds) throws BadRequestException, EntityNotFoundException, ImmutableEntityException;

    LoadBalancer updateAccessList(LoadBalancer rLb) throws EntityNotFoundException, ImmutableEntityException, BadRequestException, UnprocessableEntityException;

    Set<AccessList> diffRequestAccessListWithDomainAccessList(LoadBalancer rLb, LoadBalancer dLb);

    LoadBalancer markForDeletionAccessList(LoadBalancer rLb) throws EntityNotFoundException, ImmutableEntityException, DeletedStatusException, UnprocessableEntityException;

    LoadBalancer markForDeletionNetworkItem(LoadBalancer rLb) throws EntityNotFoundException, ImmutableEntityException;
}
