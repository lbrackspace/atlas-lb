package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.*;
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
public class MetadataRepository {
    final Log LOG = LogFactory.getLog(MetadataRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    public Set<Meta> addMetas(LoadBalancer loadBalancer, Collection<Meta> metas) {
        Set<Meta> newMetas = new HashSet<Meta>();

        for (Meta meta : metas) {
            meta.setLoadbalancer(loadBalancer);
            newMetas.add(entityManager.merge(meta));
        }

        loadBalancer.setUpdated(Calendar.getInstance());
        loadBalancer = entityManager.merge(loadBalancer);
        entityManager.flush();
        return newMetas;
    }

    public List<Meta> getMetadataByAccountIdLoadBalancerId(Integer accountId, Integer loadBalancerId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Meta> criteria = builder.createQuery(Meta.class);
        Root<Meta> metaRoot = criteria.from(Meta.class);

        LoadBalancer lb = new LoadBalancer();
        lb.setId(loadBalancerId);
        lb.setAccountId(accountId);

        Predicate belongsToLoadBalancer = builder.equal(metaRoot.get(Meta_.loadbalancer), lb);

        criteria.select(metaRoot);
        criteria.where(belongsToLoadBalancer);
        return entityManager.createQuery(criteria).getResultList();
    }
}
