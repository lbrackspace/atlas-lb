package org.openstack.atlas.service.domain.usage.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.UsageEvent;
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
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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

    public List<LoadBalancerMergedHostUsage> getAllUsageRecordsInOrderBeforeOrEqualToTime(Calendar timestamp) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LoadBalancerMergedHostUsage> criteria = builder.createQuery(LoadBalancerMergedHostUsage.class);
        Root<LoadBalancerMergedHostUsage> lbMergedHostUsageRoot = criteria.from(LoadBalancerMergedHostUsage.class);

        Predicate pollTimeBeforeTime = builder.lessThanOrEqualTo(lbMergedHostUsageRoot.get(LoadBalancerMergedHostUsage_.pollTime), timestamp);
        Order startTimeOrder = builder.asc(lbMergedHostUsageRoot.get(LoadBalancerMergedHostUsage_.pollTime));

        criteria.select(lbMergedHostUsageRoot);
        criteria.where(pollTimeBeforeTime);
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
        Query query = entityManager.createNativeQuery("SELECT u.id, u.loadbalancer_id, u.concurrent_connections, u.incoming_transfer, " +
                "u.outgoing_transfer, u.concurrent_connections_ssl, u.incoming_transfer_ssl, u.outgoing_transfer_ssl, u.poll_time, " +
                "u.num_vips, u.tags_bitmask, u.event_type, u.account_id" +
                " FROM lb_merged_host_usage u WHERE u.loadbalancer_id = :loadBalancerId" +
                " ORDER BY u.poll_time DESC LIMIT 1")
                .setParameter("loadBalancerId", lbId);

        final List<Object[]> resultList = query.getResultList();

        if (resultList == null || resultList.isEmpty()) {
            String message = "No recent usage record found.";
            LOG.debug(message);
            throw new EntityNotFoundException(message);
        }

        return rowToUsage(resultList.get(0));
    }

    private LoadBalancerMergedHostUsage rowToUsage(Object[] row) {
        Long pollTimeMillis = ((Timestamp) row[8]).getTime();
        Calendar pollTimeCal = new GregorianCalendar();
        pollTimeCal.setTimeInMillis(pollTimeMillis);

        LoadBalancerMergedHostUsage usageItem = new LoadBalancerMergedHostUsage();
        usageItem.setId((Long) row[0]);
        usageItem.setLoadbalancerId((Integer) row[1]);
        usageItem.setConcurrentConnections(((BigInteger) row[2]).longValue());
        usageItem.setIncomingTransfer(((BigInteger) row[3]).longValue());
        usageItem.setOutgoingTransfer(((BigInteger) row[4]).longValue());
        usageItem.setConcurrentConnectionsSsl(((BigInteger) row[5]).longValue());
        usageItem.setIncomingTransferSsl(((BigInteger) row[6]).longValue());
        usageItem.setOutgoingTransferSsl(((BigInteger) row[7]).longValue());
        usageItem.setPollTime(pollTimeCal);
        usageItem.setNumVips((Integer) row[9]);
        usageItem.setTagsBitmask((Integer) row[10]);
        usageItem.setEventType(null);
        if(row[11] != null) {
            usageItem.setEventType(UsageEvent.valueOf((String)row[11]));
        }
        usageItem.setAccountId((Integer) row[12]);
        return usageItem;
    }

    public void batchCreate(Collection<LoadBalancerMergedHostUsage> usages) {
        LOG.info(String.format("batchCreate() called with %d records", usages.size()));
        if(usages.isEmpty()) {
            return;
        }
        String query = generateBatchInsertQuery(usages);
        entityManager.createNativeQuery(query).executeUpdate();
    }

    public void batchDelete(Collection<LoadBalancerMergedHostUsage> usages) {
        List<Long> usageIds = new ArrayList<Long>();

        for (LoadBalancerMergedHostUsage usage : usages) {
            usageIds.add(usage.getId());
        }

        entityManager.createQuery("DELETE LoadBalancerMergedHostUsage e WHERE e.id in (:ids)")
                .setParameter("ids", usageIds)
                .executeUpdate();
    }

    private String generateBatchInsertQuery(Collection<LoadBalancerMergedHostUsage> usages) {
        final StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO lb_merged_host_usage(account_id, loadbalancer_id, outgoing_transfer, incoming_transfer, outgoing_transfer_ssl, incoming_transfer_ssl, concurrent_connections, concurrent_connections_ssl, num_vips, tags_bitmask, poll_time, event_type) values");
        sb.append(generateFormattedValues(usages));
        return sb.toString();
    }

    private String generateFormattedValues(Collection<LoadBalancerMergedHostUsage> usages) {
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
