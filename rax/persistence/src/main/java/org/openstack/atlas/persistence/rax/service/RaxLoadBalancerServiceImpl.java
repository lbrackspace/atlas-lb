package org.openstack.atlas.persistence.rax.service;

import org.openstack.atlas.service.domain.services.impl.LoadBalancerServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class RaxLoadBalancerServiceImpl extends LoadBalancerServiceImpl {
    @Override
    public org.openstack.atlas.service.domain.entities.LoadBalancer create(org.openstack.atlas.service.domain.entities.LoadBalancer loadBalancer) {
        return null;
    }
}
