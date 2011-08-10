package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.entity.LoadBalancer;

public interface LoadBalancerService {
    public LoadBalancer create(LoadBalancer loadBalancer) throws Exception;
}
