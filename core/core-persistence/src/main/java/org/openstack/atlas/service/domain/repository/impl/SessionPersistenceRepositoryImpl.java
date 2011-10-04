package org.openstack.atlas.service.domain.repository.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.SessionPersistence;
import org.openstack.atlas.service.domain.entity.SessionPersistence_;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.SessionPersistenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@Repository
@Transactional
public class SessionPersistenceRepositoryImpl implements SessionPersistenceRepository {
    final Log LOG = LogFactory.getLog(SessionPersistenceRepositoryImpl.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    @Autowired
    private LoadBalancerRepository loadBalancerRepository;

    @Override
    public SessionPersistence getSessionPersistenceByLoadBalancerId(Integer loadBalancerId) throws EntityNotFoundException {
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
        } catch (Exception e) {
            LOG.error(e);
            throw new EntityNotFoundException("No session persistence found");
        }
    }
}
