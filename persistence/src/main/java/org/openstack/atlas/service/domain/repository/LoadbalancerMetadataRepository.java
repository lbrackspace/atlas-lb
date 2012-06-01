package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.util.Constants;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

@Repository
@Transactional
public class LoadbalancerMetadataRepository {
    final Log LOG = LogFactory.getLog(LoadbalancerMetadataRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    public Set<LoadbalancerMeta> addLoadbalancerMetas(LoadBalancer loadBalancer, Collection<LoadbalancerMeta> loadbalancerMetas) {
        Set<LoadbalancerMeta> newLoadbalancerMetas = new HashSet<LoadbalancerMeta>();

        for (LoadbalancerMeta loadbalancerMeta : loadbalancerMetas) {
            loadbalancerMeta.setLoadbalancer(loadBalancer);
            newLoadbalancerMetas.add(entityManager.merge(loadbalancerMeta));
        }

        loadBalancer.setUpdated(Calendar.getInstance());
        loadBalancer = entityManager.merge(loadBalancer);
        entityManager.flush();
        return newLoadbalancerMetas;
    }

    public List<LoadbalancerMeta> getLoadbalancerMetadataByAccountIdLoadBalancerId(Integer accountId, Integer loadBalancerId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LoadbalancerMeta> criteria = builder.createQuery(LoadbalancerMeta.class);
        Root<LoadbalancerMeta> metaRoot = criteria.from(LoadbalancerMeta.class);

        LoadBalancer lb = new LoadBalancer();
        lb.setId(loadBalancerId);
        lb.setAccountId(accountId);

        Predicate belongsToLoadBalancer = builder.equal(metaRoot.get(LoadbalancerMeta_.loadbalancer), lb);

        criteria.select(metaRoot);
        criteria.where(belongsToLoadBalancer);
        return entityManager.createQuery(criteria).getResultList();
    }

    public LoadbalancerMeta getLoadbalancerMeta(Integer accountId, Integer loadBalancerId, Integer id) throws EntityNotFoundException {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LoadbalancerMeta> criteria = builder.createQuery(LoadbalancerMeta.class);
        Root<LoadbalancerMeta> metaRoot = criteria.from(LoadbalancerMeta.class);

        LoadBalancer lb = new LoadBalancer();
        lb.setId(loadBalancerId);
        lb.setAccountId(accountId);

        Predicate belongsToLoadBalancer = builder.equal(metaRoot.get(LoadbalancerMeta_.loadbalancer), lb);
        Predicate hasId = builder.equal(metaRoot.get(LoadbalancerMeta_.id), id);

        criteria.select(metaRoot);
        criteria.where(builder.and(belongsToLoadBalancer, hasId));
        final List<LoadbalancerMeta> resultList = entityManager.createQuery(criteria).getResultList();

        if (resultList.isEmpty()) {
            String message = Constants.MetaNotFound;
            LOG.warn(message);
            throw new EntityNotFoundException(message);
        }

        return resultList.get(0);
    }

    public void deleteLoadbalancerMeta(LoadBalancer loadBalancer, Integer id) throws EntityNotFoundException {
        Set<LoadbalancerMeta> dbMetadata = new HashSet<LoadbalancerMeta>(loadBalancer.getLoadbalancerMetadata());
        Boolean removed = false;

        for (LoadbalancerMeta loadbalancerMeta : dbMetadata) {
            Integer metaId = loadbalancerMeta.getId();
            if (metaId.equals(id)) {
                loadBalancer.getLoadbalancerMetadata().remove(loadbalancerMeta);
                removed = true;
            }
        }

        if (!removed) {
            String message = Constants.MetaNotFound;
            LOG.warn(message);
            throw new EntityNotFoundException(message);
        }

        loadBalancer.setUpdated(Calendar.getInstance());
        loadBalancer = entityManager.merge(loadBalancer);
        entityManager.flush();
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

    public LoadBalancer deleteLoadbalancerMetadata(LoadBalancer lb, Collection<Integer> ids) {
        Set<LoadbalancerMeta> metasCurrentlyOnLb = new HashSet<LoadbalancerMeta>(lb.getLoadbalancerMetadata());
        for (LoadbalancerMeta loadbalancerMetaCurrentlyOnLb : metasCurrentlyOnLb) {
            for (Integer idOfMetaToDelete : ids) {
                if (loadbalancerMetaCurrentlyOnLb.getId().equals(idOfMetaToDelete)) {
                    lb.getLoadbalancerMetadata().remove(loadbalancerMetaCurrentlyOnLb);
                }
            }
        }
        lb.setUpdated(Calendar.getInstance());
        lb = entityManager.merge(lb);
        entityManager.flush();
        return lb;
    }
}
