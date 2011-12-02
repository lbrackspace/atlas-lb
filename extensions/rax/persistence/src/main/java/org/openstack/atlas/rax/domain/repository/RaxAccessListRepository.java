package org.openstack.atlas.rax.domain.repository;

import org.openstack.atlas.rax.domain.entity.RaxAccessList;
import org.openstack.atlas.service.domain.exception.DeletedStatusException;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RaxAccessListRepository {

    public RaxAccessList getNetworkItemByAccountIdLoadBalancerIdNetworkItemId(Integer aid, Integer lid, Integer nid) throws EntityNotFoundException;

    public List<RaxAccessList> getAccessListByAccountIdLoadBalancerId(int accountId, int loadbalancerId, Integer offset, Integer limit, Integer markers) throws EntityNotFoundException, DeletedStatusException;
}
