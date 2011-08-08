package org.openstack.atlas.persistence.rax.repository;

import org.openstack.atlas.persistence.rax.entities.LoadBalancer;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class RaxLoadBalancerRepository extends org.openstack.atlas.service.domain.repository.LoadBalancerRepository {
    @Override
    public org.openstack.atlas.service.domain.entities.LoadBalancer create(org.openstack.atlas.service.domain.entities.LoadBalancer loadBalancer) {
        return null;
    }
}
