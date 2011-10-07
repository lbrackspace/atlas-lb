package org.openstack.atlas.service.domain.repository.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@Repository
@Transactional
public class UsageRepositoryImpl implements UsageRepository {
final Log LOG = LogFactory.getLog(UsageRepositoryImpl.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    @Override
    public List<UsageRecord> getByLoadBalancerId(Integer loadBalancerId) throws EntityNotFoundException {
        LoadBalancer loadBalancer = new LoadBalancer();
        loadBalancer.setId(loadBalancerId);

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UsageRecord> criteria = builder.createQuery(UsageRecord.class);
        Root<UsageRecord> usageRecordRoot = criteria.from(UsageRecord.class);
        Predicate belongsToLoadBalancer = builder.equal(usageRecordRoot.get(UsageRecord_.loadBalancer), loadBalancer);

        criteria.select(usageRecordRoot);
        criteria.where(belongsToLoadBalancer);

        return entityManager.createQuery(criteria).getResultList();
    }
}
