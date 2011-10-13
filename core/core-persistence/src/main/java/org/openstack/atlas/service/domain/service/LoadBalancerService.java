package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;

import java.util.List;

public interface LoadBalancerService {
    LoadBalancer create(LoadBalancer loadBalancer) throws PersistenceServiceException;

    LoadBalancer update(LoadBalancer loadBalancer) throws PersistenceServiceException;

    void preDelete(Integer accountId, List<Integer> loadBalancerIds) throws PersistenceServiceException;

    void preDelete(Integer accountId, Integer loadBalancerId) throws PersistenceServiceException;

    void delete(LoadBalancer lb) throws PersistenceServiceException;
}
