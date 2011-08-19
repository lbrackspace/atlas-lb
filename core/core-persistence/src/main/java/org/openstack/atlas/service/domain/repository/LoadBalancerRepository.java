package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.common.Constants;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.Calendar;
import java.util.Set;

@Repository
@Transactional
public class LoadBalancerRepository {

    final Log LOG = LogFactory.getLog(LoadBalancerRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    public LoadBalancer getById(Integer id) throws EntityNotFoundException {
        LoadBalancer loadBalancer = entityManager.find(LoadBalancer.class, id);
        if (loadBalancer == null) {
            String message = Constants.LoadBalancerNotFound;
            LOG.warn(message);
            throw new EntityNotFoundException(message);
        }
        return loadBalancer;
    }

    public LoadBalancer getByIdAndAccountId(Integer id, Integer accountId) throws EntityNotFoundException {
        LoadBalancer loadBalancer = getById(id);
        if (!loadBalancer.getAccountId().equals(accountId)) {
            String message = Constants.LoadBalancerNotFound;
            LOG.warn(message);
            throw new EntityNotFoundException(message);
        }

        return loadBalancer;
    }

    public LoadBalancer create(LoadBalancer loadBalancer) {

        final Set<LoadBalancerJoinVip> lbJoinVipsToLink = loadBalancer.getLoadBalancerJoinVipSet();
        loadBalancer.setLoadBalancerJoinVipSet(null);
        final Set<LoadBalancerJoinVip6> lbJoinVip6sToLink = loadBalancer.getLoadBalancerJoinVip6Set();
        loadBalancer.setLoadBalancerJoinVip6Set(null);

        loadBalancer = setLbIdOnChildObjects(loadBalancer);

        loadBalancer.setCreated(Calendar.getInstance());
        loadBalancer.setUpdated(Calendar.getInstance());
        loadBalancer = entityManager.merge(loadBalancer);

        // Now attach loadbalancer to vips
        for (LoadBalancerJoinVip lbJoinVipToLink : lbJoinVipsToLink) {
            entityManager.merge(lbJoinVipToLink.getVirtualIp());
            LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip(loadBalancer.getPort(), loadBalancer, lbJoinVipToLink.getVirtualIp());
            entityManager.merge(loadBalancerJoinVip);
        }

        loadBalancer.setLoadBalancerJoinVip6Set(lbJoinVip6sToLink);

        entityManager.flush();
        return loadBalancer;
    }

    private LoadBalancer setLbIdOnChildObjects(final LoadBalancer loadBalancer) {
        if (loadBalancer.getNodes() != null) {
            for (Node node : loadBalancer.getNodes()) {
                node.setLoadBalancer(loadBalancer);
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

    public LoadBalancer update(LoadBalancer loadBalancer) {
        final Set<LoadBalancerJoinVip> lbJoinVipsToLink = loadBalancer.getLoadBalancerJoinVipSet();
        loadBalancer.setLoadBalancerJoinVipSet(null);

        loadBalancer.setUpdated(Calendar.getInstance());
        loadBalancer = entityManager.merge(loadBalancer);

        // Now attach loadbalancer to vips
        for (LoadBalancerJoinVip lbJoinVipToLink : lbJoinVipsToLink) {
            VirtualIp virtualIp = entityManager.find(VirtualIp.class, lbJoinVipToLink.getVirtualIp().getId());
            LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip(loadBalancer.getPort(), loadBalancer, virtualIp);
            entityManager.merge(loadBalancerJoinVip);
            entityManager.merge(lbJoinVipToLink.getVirtualIp());
        }

        entityManager.flush();
        return loadBalancer;
    }
}
