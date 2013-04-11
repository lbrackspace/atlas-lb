package org.openstack.atlas.service.domain.usage.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage_;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Repository
@Transactional(value = "usage")
public class LoadBalancerMergedHostUsageRepository {

    final Log LOG = LogFactory.getLog(LoadBalancerMergedHostUsageRepository.class);
    @PersistenceContext(unitName = "loadbalancingUsage")
    private EntityManager entityManager;

    public List<LoadBalancerMergedHostUsage> getAllUsageRecordsInOrder() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LoadBalancerMergedHostUsage> criteria = builder.createQuery(LoadBalancerMergedHostUsage.class);
        Root<LoadBalancerMergedHostUsage> usageEventRoot = criteria.from(LoadBalancerMergedHostUsage.class);

        Order startTimeOrder = builder.asc(usageEventRoot.get(LoadBalancerMergedHostUsage_.pollTime));

        criteria.select(usageEventRoot);
        criteria.orderBy(startTimeOrder);

        List<LoadBalancerMergedHostUsage> usageEvents = entityManager.createQuery(criteria).getResultList();
        return (usageEvents == null) ? new ArrayList<LoadBalancerMergedHostUsage>() : usageEvents;
    }

    public void batchDelete(Collection<LoadBalancerMergedHostUsage> usages) {
        List<Integer> usageIds = new ArrayList<Integer>();

        for (LoadBalancerMergedHostUsage usage : usages) {
            usageIds.add(usage.getId());
        }

        entityManager.createQuery("DELETE LoadBalancerMergedHostUsage e WHERE e.id in (:ids)")
                .setParameter("ids", usageIds)
                .executeUpdate();
    }
}
