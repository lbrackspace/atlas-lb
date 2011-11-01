package org.openstack.atlas.service.domain.repository.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.HealthMonitorRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@Repository
@Transactional
public class HealthMonitorRepositoryImpl implements HealthMonitorRepository {
    final Log LOG = LogFactory.getLog(HealthMonitorRepositoryImpl.class);
    private static final String entityNotFound = "Health monitor not found";
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    @Override
    public HealthMonitor getByLoadBalancerId(Integer loadBalancerId) throws EntityNotFoundException {
        LoadBalancer loadBalancer = new LoadBalancer();
        loadBalancer.setId(loadBalancerId);

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<HealthMonitor> criteria = builder.createQuery(HealthMonitor.class);
        Root<HealthMonitor> monitorRoot = criteria.from(HealthMonitor.class);
        Predicate belongsToLoadBalancer = builder.equal(monitorRoot.get(HealthMonitor_.loadBalancer), loadBalancer);

        criteria.select(monitorRoot);
        criteria.where(belongsToLoadBalancer);

        try {
            return entityManager.createQuery(criteria).setMaxResults(1).getSingleResult();
        } catch (NoResultException e) {
            throw new EntityNotFoundException(entityNotFound);
        } catch (NonUniqueResultException e) {
            LOG.error("More than one health monitor detected!", e);
            throw new EntityNotFoundException(entityNotFound);
        }
    }

    @Override
    public void delete(HealthMonitor healthMonitor) throws EntityNotFoundException {
        if (healthMonitor == null) throw new EntityNotFoundException(entityNotFound);
        healthMonitor = entityManager.merge(healthMonitor); // Re-attach hibernate instance
        entityManager.remove(healthMonitor);
    }
}
