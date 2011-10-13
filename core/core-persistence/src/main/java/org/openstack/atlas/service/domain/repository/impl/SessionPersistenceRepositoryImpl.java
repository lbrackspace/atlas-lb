package org.openstack.atlas.service.domain.repository.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.SessionPersistenceRepository;
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
public class SessionPersistenceRepositoryImpl implements SessionPersistenceRepository {
    final Log LOG = LogFactory.getLog(SessionPersistenceRepositoryImpl.class);
    private static final String entityNotFound = "Session persistence not found";
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    @Override
    public SessionPersistence getByLoadBalancerId(Integer loadBalancerId) throws EntityNotFoundException {
        LoadBalancer loadBalancer = new LoadBalancer();
        loadBalancer.setId(loadBalancerId);

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SessionPersistence> criteria = builder.createQuery(SessionPersistence.class);
        Root<SessionPersistence> persistenceRoot = criteria.from(SessionPersistence.class);
        Predicate belongsToLoadBalancer = builder.equal(persistenceRoot.get(SessionPersistence_.loadBalancer), loadBalancer);

        criteria.select(persistenceRoot);
        criteria.where(belongsToLoadBalancer);

        try {
            return entityManager.createQuery(criteria).setMaxResults(1).getSingleResult();
        } catch (NoResultException e) {
            throw new EntityNotFoundException(entityNotFound);
        } catch (NonUniqueResultException e) {
            LOG.error("More than one session persistence detected!", e);
            throw new EntityNotFoundException(entityNotFound);
        }
    }

    @Override
    public void delete(SessionPersistence sessionPersistence) throws EntityNotFoundException {
        if (sessionPersistence == null) throw new EntityNotFoundException(entityNotFound);
        sessionPersistence = entityManager.merge(sessionPersistence); // Re-attach hibernate instance
        entityManager.remove(sessionPersistence);
    }
}
