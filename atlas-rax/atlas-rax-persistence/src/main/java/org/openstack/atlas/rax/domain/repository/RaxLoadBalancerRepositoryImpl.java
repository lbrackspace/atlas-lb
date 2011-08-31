package org.openstack.atlas.rax.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.rax.domain.entity.AccessList;
import org.openstack.atlas.rax.domain.entity.RaxLoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.Node;
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
    protected LoadBalancer setLbIdOnChildObjects(final LoadBalancer loadBalancer) {
        if (loadBalancer.getNodes() != null) {
            for (Node node : loadBalancer.getNodes()) {
                node.setLoadBalancer(loadBalancer);
            }
        }

        if (loadBalancer instanceof RaxLoadBalancer) {
            if (((RaxLoadBalancer) loadBalancer).getAccessLists() != null) {
                for (AccessList accessList : ((RaxLoadBalancer) loadBalancer).getAccessLists()) {
                    accessList.setLoadbalancer(((RaxLoadBalancer) loadBalancer));
                }
            }
        }

        if (loadBalancer.getConnectionThrottle() != null) {
            loadBalancer.getConnectionThrottle().setLoadBalancer(loadBalancer);
        }
        if (loadBalancer.getHealthMonitor() != null) {
            loadBalancer.getHealthMonitor().setLoadBalancer(loadBalancer);
        }
        return loadBalancer;
    }
}
