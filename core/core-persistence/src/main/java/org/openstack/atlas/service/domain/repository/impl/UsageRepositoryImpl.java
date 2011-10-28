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
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    @Override
    public List<UsageRecord> getMostRecentUsageForLoadBalancers(Set<Integer> lbIds) {
        if (lbIds == null || lbIds.isEmpty()) return new ArrayList<UsageRecord>();

        Query query = entityManager.createNativeQuery("SELECT a.* " +
                "FROM load_balancer_usage a, " +
                "(SELECT load_balancer_id, max(end_time) as end_time FROM load_balancer_usage WHERE load_balancer_id in (:lbIds) GROUP BY load_balancer_id) b " +
                "WHERE a.load_balancer_id in (:lbIds) and a.load_balancer_id = b.load_balancer_id and a.end_time = b.end_time;", UsageRecord.class)
                .setParameter("lbIds", lbIds);

        List<UsageRecord> usage = (List<UsageRecord>) query.getResultList();
        if (usage == null) return new ArrayList<UsageRecord>();

        return usage;
    }

    @Override
    public void batchCreate(List<UsageRecord> recordsToInsert) {
        // TODO: Implement
    }

    @Override
    public void batchUpdate(List<UsageRecord> recordsToUpdate) {
        // TODO: Implement
    }
}
