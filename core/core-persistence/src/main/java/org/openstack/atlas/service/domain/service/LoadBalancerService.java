package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;

import java.util.List;

public interface LoadBalancerService {
    LoadBalancer create(LoadBalancer loadBalancer) throws PersistenceServiceException;
}
