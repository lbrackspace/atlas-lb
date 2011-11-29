package org.openstack.atlas.rax.domain.service;

import org.openstack.atlas.core.api.v1.LoadBalancer;
import org.openstack.atlas.rax.domain.entity.RaxAccessList;
import org.openstack.atlas.service.domain.exception.BadRequestException;
import org.openstack.atlas.service.domain.exception.DeletedStatusException;
import org.openstack.atlas.service.domain.exception.ImmutableEntityException;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Set;

@Service
public interface RaxAccessListService {

    public List<RaxAccessList> getAccessListByAccountIdLoadBalancerId(int accountId, int loadbalancerId, Integer offset, Integer limit, Integer marker) throws EntityNotFoundException, DeletedStatusException, org.openstack.atlas.service.domain.exception.EntityNotFoundException;

    public LoadBalancer markForDeletionNetworkItems(LoadBalancer returnLB, List<Integer> networkItemIds) throws BadRequestException, EntityNotFoundException, ImmutableEntityException;

    public LoadBalancer updateAccessList(LoadBalancer rLb) throws EntityNotFoundException, ImmutableEntityException, BadRequestException, UnprocessableEntityException;

    public Set<RaxAccessList> diffRequestAccessListWithDomainAccessList(LoadBalancer rLb, org.openstack.atlas.service.domain.entity.LoadBalancer dLb);

    public LoadBalancer markForDeletionAccessList(LoadBalancer rLb) throws EntityNotFoundException, ImmutableEntityException, DeletedStatusException, UnprocessableEntityException;

    public LoadBalancer markForDeletionNetworkItem(LoadBalancer rLb) throws EntityNotFoundException, ImmutableEntityException;

}
