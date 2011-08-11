package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;

public interface LoadBalancerService {

    LoadBalancer get(Integer id) throws EntityNotFoundException;

    LoadBalancer get(Integer id, Integer accountId) throws EntityNotFoundException;

    LoadBalancer create(LoadBalancer loadBalancer) throws Exception;

    LoadBalancer update(LoadBalancer lb) throws Exception;
}
