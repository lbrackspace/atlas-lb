package org.openstack.atlas.rax.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.rax.domain.entity.AccessList;
import org.openstack.atlas.rax.domain.entity.RaxLoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip6;
import org.openstack.atlas.service.domain.entity.Node;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

@Repository
@Transactional
public class RaxLoadBalancerRepository extends LoadBalancerRepository {

    final Log LOG = LogFactory.getLog(RaxLoadBalancerRepository.class);
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

/*    @Override
    public LoadBalancer create(LoadBalancer loadBalancer) {

        final Set<LoadBalancerJoinVip> lbJoinVipsToLink = loadBalancer.getLoadBalancerJoinVipSet();
        loadBalancer.setLoadBalancerJoinVipSet(null);
        final Set<LoadBalancerJoinVip6> lbJoinVip6sToLink = loadBalancer.getLoadBalancerJoinVip6Set();
        loadBalancer.setLoadBalancerJoinVip6Set(null);

        loadBalancer = setLbIdOnChildObjects(loadBalancer);

        Calendar current = Calendar.getInstance();
        loadBalancer.setCreated(current);
        loadBalancer.setUpdated(current);
        loadBalancer = entityManager.merge(loadBalancer);

        // Now attach loadbalancer to vips
        for (LoadBalancerJoinVip lbJoinVipToLink : lbJoinVipsToLink) {
            entityManager.merge(lbJoinVipToLink.getVirtualIp());
            LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip(loadBalancer.getPort(), loadBalancer, lbJoinVipToLink.getVirtualIp());
            entityManager.merge(loadBalancerJoinVip);
        }

*//*        for(LoadBalancerJoinVip6 lbJoinVipToLink : lbJoinVip6sToLink) {
            LoadBalancerJoinVip6 jv = new LoadBalancerJoinVip6(loadBalancer.getPort(), loadBalancer, lbJoinVipToLink.getVirtualIp());
            entityManager.persist(jv);
        }*//*

        loadBalancer.setLoadBalancerJoinVip6Set(lbJoinVip6sToLink);
        entityManager.flush();

        Set<LoadBalancerJoinVip6> loadBalancerJoinVip6SetConfig = loadBalancer.getLoadBalancerJoinVip6Set();
        loadBalancer.setLoadBalancerJoinVip6Set(null);
        Set<LoadBalancerJoinVip6> newLbVip6Setconfig = new HashSet<LoadBalancerJoinVip6>();
        loadBalancer.setLoadBalancerJoinVip6Set(newLbVip6Setconfig);
        for (LoadBalancerJoinVip6 jv6 : loadBalancerJoinVip6SetConfig) {
            LoadBalancerJoinVip6 jv = new LoadBalancerJoinVip6(loadBalancer.getPort(), loadBalancer, jv6.getVirtualIp());
            entityManager.persist(jv);
        }
        return loadBalancer;
    }*/
}
