package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Meta;
import org.openstack.atlas.service.domain.entities.Meta_;
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

    public Meta getMeta(Integer accountId, Integer loadBalancerId, Integer id) throws EntityNotFoundException {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Meta> criteria = builder.createQuery(Meta.class);
        Root<Meta> metaRoot = criteria.from(Meta.class);

        LoadBalancer lb = new LoadBalancer();
        lb.setId(loadBalancerId);
        lb.setAccountId(accountId);

        Predicate belongsToLoadBalancer = builder.equal(metaRoot.get(Meta_.loadbalancer), lb);
        Predicate hasId = builder.equal(metaRoot.get(Meta_.id), id);

        criteria.select(metaRoot);
        criteria.where(builder.and(belongsToLoadBalancer, hasId));
        final List<Meta> resultList = entityManager.createQuery(criteria).getResultList();

        if (resultList.isEmpty()) {
            String message = Constants.MetaNotFound;
            LOG.warn(message);
            throw new EntityNotFoundException(message);
        }

        return resultList.get(0);
    }

    public void deleteMeta(LoadBalancer loadBalancer, Integer id) throws EntityNotFoundException {
        Set<Meta> dbMetadata = new HashSet<Meta>(loadBalancer.getMetadata());
        Boolean removed = false;

        for (Meta meta : dbMetadata) {
            Integer metaId = meta.getId();
            if (metaId.equals(id)) {
                loadBalancer.getMetadata().remove(meta);
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
}
