package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;
import org.springframework.transaction.annotation.Transactional;

public interface LoadBalancerService {
    LoadBalancer create(LoadBalancer loadBalancer) throws PersistenceServiceException;

    LoadBalancer get(Integer id) throws EntityNotFoundException;

    LoadBalancer update(LoadBalancer lb) throws Exception;

    LoadBalancer get(Integer id, Integer accountId) throws EntityNotFoundException;
}
