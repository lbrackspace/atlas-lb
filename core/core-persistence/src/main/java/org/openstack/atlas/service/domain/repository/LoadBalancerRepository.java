package org.openstack.atlas.service.domain.repository;

import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;

import java.util.List;

public interface LoadBalancerRepository {
    LoadBalancer getById(Integer id) throws EntityNotFoundException;

    LoadBalancer getByIdAndAccountId(Integer id, Integer accountId) throws EntityNotFoundException;

    List<LoadBalancer> getByAccountId(Integer accountId);

    LoadBalancer create(LoadBalancer loadBalancer);

    LoadBalancer update(LoadBalancer loadBalancer);

    Integer getNumNonDeletedLoadBalancersForAccount(Integer accountId);
}
