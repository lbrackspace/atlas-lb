package org.openstack.atlas.service.domain.usage.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.JobState_;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsageEvent;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsageEvent_;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Repository
@Transactional(value = "usage")
public class LoadBalancerUsageEventRepository {

    final Log LOG = LogFactory.getLog(LoadBalancerUsageEventRepository.class);
    @PersistenceContext(unitName = "loadbalancingUsage")
    private EntityManager entityManager;

    public List<LoadBalancerUsageEvent> getAllUsageEventEntriesInOrder() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LoadBalancerUsageEvent> criteria = builder.createQuery(LoadBalancerUsageEvent.class);
        Root<LoadBalancerUsageEvent> usageEventRoot = criteria.from(LoadBalancerUsageEvent.class);

        Order startTimeOrder = builder.asc(usageEventRoot.get(LoadBalancerUsageEvent_.startTime));

        criteria.select(usageEventRoot);
        criteria.orderBy(startTimeOrder);

        List<LoadBalancerUsageEvent> usageEvents = entityManager.createQuery(criteria).getResultList();
        return (usageEvents == null) ? new ArrayList<LoadBalancerUsageEvent>() : usageEvents;
    }

    public void batchDelete(Collection<LoadBalancerUsageEvent> usages) {
        List<Integer> usageIds = new ArrayList<Integer>();

        for (LoadBalancerUsageEvent usage : usages) {
            usageIds.add(usage.getId());
        }

        entityManager.createQuery("DELETE LoadBalancerUsageEvent e WHERE e.id in (:ids)")
                .setParameter("ids", usageIds)
                .executeUpdate();
    }

    public void batchCreate(List<LoadBalancerUsageEvent> usages) {
        LOG.info(String.format("batchCreate() called with %d records", usages.size()));
        String query = generateBatchInsertQuery(usages);
        entityManager.createNativeQuery(query).executeUpdate();
    }

    private String generateBatchInsertQuery(List<LoadBalancerUsageEvent> usages) {
        final StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO lb_usage_event(account_id, loadbalancer_id, start_time, num_vips, event_type) values");
        sb.append(generateFormattedValues(usages));
        return sb.toString();
    }

    private String generateFormattedValues(List<LoadBalancerUsageEvent> usages) {
        StringBuilder sb = new StringBuilder();

        for (LoadBalancerUsageEvent usage : usages) {
            sb.append("(");
            if (usage.getId() != null) {
                sb.append(usage.getId()).append(",");
            }
            sb.append(usage.getAccountId()).append(",");
            sb.append(usage.getLoadbalancerId()).append(",");

            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String startTime = formatter.format(usage.getStartTime().getTime());
            sb.append("'").append(startTime).append("',");

            sb.append(usage.getNumVips()).append(",");
            if (usage.getEventType() == null) {
                sb.append(usage.getEventType());
            } else {
                sb.append("'").append(usage.getEventType()).append("'");
            }
            sb.append("),");

        }
        if (sb.toString().endsWith(",")) {
            sb.deleteCharAt(sb.lastIndexOf(","));
        }
        return sb.toString();
    }

    public void create(LoadBalancerUsageEvent loadBalancerUsageEvent) {
        entityManager.persist(loadBalancerUsageEvent);
    }

    public List<LoadBalancerUsageEvent> getEventsForLoadBalancer(Integer loadBalancerId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LoadBalancerUsageEvent> criteria = builder.createQuery(LoadBalancerUsageEvent.class);
        Root<LoadBalancerUsageEvent> usageEventRoot = criteria.from(LoadBalancerUsageEvent.class);

        Predicate hasLoadBalancerId = builder.equal(usageEventRoot.get(LoadBalancerUsageEvent_.loadbalancerId), loadBalancerId);

        criteria.select(usageEventRoot);
        criteria.where(hasLoadBalancerId);

        List<LoadBalancerUsageEvent> usageEvents = entityManager.createQuery(criteria).getResultList();
        return usageEvents;
    }

    public List<LoadBalancerUsageEvent> getFilteredEventsForLoadBalancer(Integer loadBalancerId, UsageEvent usageEvent) {
        List<LoadBalancerUsageEvent> usageEvents = getEventsForLoadBalancer(loadBalancerId);
        List<LoadBalancerUsageEvent> filteredUsageEvents = new ArrayList<LoadBalancerUsageEvent>();

        for (LoadBalancerUsageEvent event : usageEvents) {
            if (event.getEventType().equals(usageEvent.name())) filteredUsageEvents.add(event);
        }

        return filteredUsageEvents;
    }
}

