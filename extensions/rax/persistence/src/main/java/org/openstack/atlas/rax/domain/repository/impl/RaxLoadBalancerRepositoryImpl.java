package org.openstack.atlas.rax.domain.repository.impl;

import org.openstack.atlas.rax.domain.entity.RaxAccessList;
import org.openstack.atlas.rax.domain.entity.RaxLoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.repository.impl.LoadBalancerRepositoryImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Primary
@Repository
@Transactional
public class RaxLoadBalancerRepositoryImpl extends LoadBalancerRepositoryImpl {

    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    @Override
    protected void setLbIdOnChildObjects(final LoadBalancer loadBalancer) {
        super.setLbIdOnChildObjects(loadBalancer);
        if (loadBalancer instanceof RaxLoadBalancer) {
            if (((RaxLoadBalancer) loadBalancer).getAccessLists() != null) {
                for (RaxAccessList accessList : ((RaxLoadBalancer) loadBalancer).getAccessLists()) {
                    accessList.setLoadbalancer(((RaxLoadBalancer) loadBalancer));
                }
            }
        }
    }
}
