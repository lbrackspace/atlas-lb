package org.openstack.atlas.service.domain.repository.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.ConnectionThrottleRepository;
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
public class ConnectionThrottleRepositoryImpl implements ConnectionThrottleRepository {
    final Log LOG = LogFactory.getLog(ConnectionThrottleRepositoryImpl.class);
    private static final String entityNotFound = "Connection throttle not found";
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    @Override
    public ConnectionThrottle getByLoadBalancerId(Integer loadBalancerId) throws EntityNotFoundException {
        LoadBalancer loadBalancer = new LoadBalancer();
        loadBalancer.setId(loadBalancerId);

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ConnectionThrottle> criteria = builder.createQuery(ConnectionThrottle.class);
        Root<ConnectionThrottle> throttleRoot = criteria.from(ConnectionThrottle.class);
        Predicate belongsToLoadBalancer = builder.equal(throttleRoot.get(ConnectionThrottle_.loadBalancer), loadBalancer);

        criteria.select(throttleRoot);
        criteria.where(belongsToLoadBalancer);

        try {
            return entityManager.createQuery(criteria).setMaxResults(1).getSingleResult();
        } catch (NoResultException e) {
            throw new EntityNotFoundException(entityNotFound);
        } catch (NonUniqueResultException e) {
            LOG.error("More than one connection throttle detected!", e);
            throw new EntityNotFoundException(entityNotFound);
        }
    }

    @Override
    public void delete(ConnectionThrottle connectionThrottle) throws EntityNotFoundException {
        if (connectionThrottle == null) throw new EntityNotFoundException(entityNotFound);
        connectionThrottle = entityManager.merge(connectionThrottle); // Re-attach hibernate instance
        entityManager.remove(connectionThrottle);
    }
}
