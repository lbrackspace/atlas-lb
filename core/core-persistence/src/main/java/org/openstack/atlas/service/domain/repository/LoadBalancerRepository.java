package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.util.Constants;
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
    private EntityManager entityManager;/**/

    public LoadBalancer getById(Integer id) throws EntityNotFoundException {
        LoadBalancer lb = entityManager.find(LoadBalancer.class, id);
        if (lb == null) {
            String message = Constants.LoadBalancerNotFound;
            LOG.warn(message);
            throw new EntityNotFoundException(message);
        }
        return lb;
    }

    public LoadBalancer getByIdAndAccountId(Integer id, Integer accountId) throws EntityNotFoundException {
        LoadBalancer lb;
        lb = getById(id);
        if (!lb.getAccountId().equals(accountId)) {
            String message = Constants.LoadBalancerNotFound;
            LOG.warn(message);
            throw new EntityNotFoundException(message);
        }

        return lb;
    }

    public LoadBalancer create(LoadBalancer loadBalancer) {
        entityManager.persist(loadBalancer);
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

    public void delete(LoadBalancer loadBalancer) {
        entityManager.remove(loadBalancer);
        entityManager.flush();
    }
}
