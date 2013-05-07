package org.openstack.atlas.service.domain.usage.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage_;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.criteria.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
        Root<LoadBalancerMergedHostUsage> lbMergedHostUsageRoot = criteria.from(LoadBalancerMergedHostUsage.class);

        Order startTimeOrder = builder.asc(lbMergedHostUsageRoot.get(LoadBalancerMergedHostUsage_.pollTime));

        criteria.select(lbMergedHostUsageRoot);
        criteria.orderBy(startTimeOrder);

        List<LoadBalancerMergedHostUsage> usageEvents = entityManager.createQuery(criteria).getResultList();
        return (usageEvents == null) ? new ArrayList<LoadBalancerMergedHostUsage>() : usageEvents;
    }

    public List<LoadBalancerMergedHostUsage> getAllUsageRecordsInOrderBeforeTime(Calendar timestamp) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LoadBalancerMergedHostUsage> criteria = builder.createQuery(LoadBalancerMergedHostUsage.class);
        Root<LoadBalancerMergedHostUsage> lbMergedHostUsageRoot = criteria.from(LoadBalancerMergedHostUsage.class);

        Predicate endTimeBeforeTime = builder.lessThan(lbMergedHostUsageRoot.get(LoadBalancerMergedHostUsage_.pollTime), timestamp);
        Order startTimeOrder = builder.asc(lbMergedHostUsageRoot.get(LoadBalancerMergedHostUsage_.pollTime));

        criteria.select(lbMergedHostUsageRoot);
        criteria.where(endTimeBeforeTime);
        criteria.orderBy(startTimeOrder);

        List<LoadBalancerMergedHostUsage> usageEvents = entityManager.createQuery(criteria).getResultList();
        return (usageEvents == null) ? new ArrayList<LoadBalancerMergedHostUsage>() : usageEvents;
    }

    public void deleteAllRecordsBefore(Calendar timestamp) {
        Query query = entityManager.createQuery("DELETE LoadBalancerMergedHostUsage u WHERE u.pollTime < :timestamp")
                .setParameter("timestamp", timestamp, TemporalType.TIMESTAMP);
        int numRowsDeleted = query.executeUpdate();
        LOG.info(String.format("Deleted %d rows with pollTime before %s", numRowsDeleted, timestamp.getTime()));
    }

    public void create(LoadBalancerMergedHostUsage loadBalancerMergedHostUsage) {
        entityManager.persist(loadBalancerMergedHostUsage);
    }

    public LoadBalancerMergedHostUsage getMostRecentRecordForLoadBalancer(int lbId) throws EntityNotFoundException{
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LoadBalancerMergedHostUsage> criteria = builder.createQuery(LoadBalancerMergedHostUsage.class);
        Root<LoadBalancerMergedHostUsage> usageRoot = criteria.from(LoadBalancerMergedHostUsage.class);

        Predicate hasLbId = builder.equal(usageRoot.get(LoadBalancerMergedHostUsage_.loadbalancerId), lbId);

        criteria.select(usageRoot);
        criteria.where(hasLbId);

        try {
            return entityManager.createQuery(criteria).getSingleResult();
        } catch (NoResultException e) {
            String message = "No recent usage record found.";
            LOG.debug(message);
            throw new EntityNotFoundException(message);
        }
    }

    public void batchCreate(List<LoadBalancerMergedHostUsage> usages) {
        LOG.info(String.format("batchCreate() called with %d records", usages.size()));

        String query = generateBatchInsertQuery(usages);
        entityManager.createNativeQuery(query).executeUpdate();
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

    private String generateBatchInsertQuery(List<LoadBalancerMergedHostUsage> usages) {
        final StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO lb_merged_host_usage(account_id, loadbalancer_id, outgoing_transfer, incoming_transfer, outgoing_transfer_ssl, incoming_transfer_ssl, concurrent_connections, concurrent_connections_ssl, num_vips, tags_bitmask, poll_time, event_type) values");
        sb.append(generateFormattedValues(usages));
        return sb.toString();
    }

    private String generateFormattedValues(List<LoadBalancerMergedHostUsage> usages) {
        StringBuilder sb = new StringBuilder();

        for (LoadBalancerMergedHostUsage usage : usages) {
            sb.append("(");
            if (usage.getId() != null) {
                sb.append(usage.getId()).append(",");
            }
            sb.append(usage.getAccountId()).append(",");
            sb.append(usage.getLoadbalancerId()).append(",");
            sb.append(usage.getOutgoingTransfer()).append(",");
            sb.append(usage.getIncomingTransfer()).append(",");
            sb.append(usage.getOutgoingTransferSsl()).append(",");
            sb.append(usage.getIncomingTransferSsl()).append(",");
            sb.append(usage.getConcurrentConnections()).append(",");
            sb.append(usage.getConcurrentConnectionsSsl()).append(",");
            sb.append(usage.getNumVips()).append(",");
            sb.append(usage.getTagsBitmask()).append(",");

            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String pollTime = formatter.format(usage.getPollTime().getTime());
            sb.append("'").append(pollTime).append("',");

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
}
