package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.entities.Usage_;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.RestApiConfiguration;

@Repository
@Transactional
public class UsageRepository {

    final Log LOG = LogFactory.getLog(UsageRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;
    private final Integer NUM_DAYS_RETENTION = 90;
    private final Integer DEFAULT_DELETE_LIMIT = 10000;

    public List<Usage> getMostRecentUsageForLoadBalancers(Collection<Integer> loadBalancerIds) {
        if (loadBalancerIds == null || loadBalancerIds.isEmpty()) return new ArrayList<Usage>();

        Query query = entityManager.createNativeQuery("SELECT a.* " +
                "FROM lb_usage a, " +
                "(SELECT loadbalancer_id, max(start_time) as start_time FROM lb_usage WHERE loadbalancer_id in (:loadbalancerIds) GROUP BY loadbalancer_id) b " +
                "WHERE a.loadbalancer_id in (:loadbalancerIds) and a.loadbalancer_id = b.loadbalancer_id and a.start_time = b.start_time;", Usage.class)
                .setParameter("loadbalancerIds", loadBalancerIds);

        List<Usage> usage = (List<Usage>) query.getResultList();
        if (usage == null || usage.isEmpty()) return new ArrayList<Usage>();

        return usage;
    }

    public Usage getMostRecentUsageForLoadBalancer(Integer loadBalancerId) throws EntityNotFoundException {
        if (loadBalancerId == null) throw new EntityNotFoundException("Lb id passed in is null.");

        Query query = entityManager.createNativeQuery("SELECT u.id, u.loadbalancer_id, u.avg_concurrent_conns, u.bandwidth_in, u.bandwidth_out, u.avg_concurrent_conns_ssl, u.bandwidth_in_ssl, u.bandwidth_out_ssl, u.start_time, u.end_time, u.num_polls, u.num_vips, u.tags_bitmask, u.event_type, u.account_id" +
                " FROM lb_usage u WHERE u.loadbalancer_id = :loadBalancerId" +
                " ORDER BY u.start_time DESC LIMIT 1")
                .setParameter("loadBalancerId", loadBalancerId);

        final List<Object[]> resultList = query.getResultList();

        if (resultList == null || resultList.isEmpty()) {
            String message = "No recent usage record found.";
            LOG.debug(message);
            throw new EntityNotFoundException(message);
        }

        return rowToUsage(resultList.get(0));
    }

    public void deleteAllRecordsBeforeOrEqualTo(Calendar time) {
        RestApiConfiguration configuration = new RestApiConfiguration();
        String limitStr = configuration.getString(PublicApiServiceConfigurationKeys.usage_deletion_limit);
        int limitInt;
        try {
            limitInt = Integer.parseInt(limitStr);
        } catch(NumberFormatException nfe) {
            limitInt = DEFAULT_DELETE_LIMIT;
        }
        int numRowsDeleted;
        int totalRowsDeleted = 0;
        int batchCount = 0;

        do {
            Query nativeQ = entityManager.createNativeQuery("DELETE FROM lb_usage WHERE end_time <= :timestamp AND needs_pushed = 0 LIMIT :limit")
                    .setParameter("timestamp", time, TemporalType.TIMESTAMP).setParameter("limit", limitInt);
            numRowsDeleted = nativeQ.executeUpdate();
            totalRowsDeleted += numRowsDeleted;
            batchCount++;
            LOG.info(String.format("Deleted %d rows with endTime before %s in batch %d.", numRowsDeleted, time.getTime(), batchCount));
        } while(numRowsDeleted > 0);

        LOG.info(String.format("Finished deleting rows. Deleted %d total rows in %d batch(es) with endTime before %s.", totalRowsDeleted, batchCount, time.getTime()));
    }

    public void deleteOldRecords() {
        Calendar deletePoint = Calendar.getInstance();
        deletePoint.add(Calendar.DATE, -NUM_DAYS_RETENTION);
        deleteAllRecordsBeforeOrEqualTo(deletePoint);
    }

    public void batchCreate(Collection<Usage> usages) {
        LOG.info(String.format("batchCreate() called with %d records", usages.size()));

        String query = generateBatchInsertQuery(usages);
        entityManager.createNativeQuery(query).executeUpdate();
    }

    public void batchUpdate(List<Usage> usages) {
        batchUpdate(usages, true);
    }

    public void batchUpdate(List<Usage> usages, boolean isUsageUpdate) {
        LOG.info(String.format("batchUpdate() called with %d records", usages.size()));

        String query = generateBatchUpdateQuery(usages, isUsageUpdate);
//        LOG.info(String.format("Query for batch update: %s", query));
//        StringBuilder sb = new StringBuilder();
//        sb.append("usage IDS[");
//        for (Usage usage : usages) {
//            sb.append(" ").append(usage.getId()).append(",");
//        }
//        sb.append("]");
//        LOG.info(sb.toString());
        entityManager.createNativeQuery(query).executeUpdate();
//        try {
//            Thread.sleep(30000);
//        } catch (InterruptedException e) {
//            LOG.error("We had an interrupted exception");
//            LOG.error(String.format("Problem in sleep thread: %s", e));
//        }
    }

    public void updatePushedRecord(Usage usageRecord) {
        LOG.info(String.format("updateEntryRecord called"));
        entityManager.merge(usageRecord);
    }

    private String generateBatchInsertQuery(Collection<Usage> usages) {
        final StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO lb_usage(loadbalancer_id, account_id, avg_concurrent_conns, bandwidth_in, bandwidth_out, avg_concurrent_conns_ssl, bandwidth_in_ssl, bandwidth_out_ssl, start_time, end_time, num_polls, num_vips, tags_bitmask, event_type, entry_version, needs_pushed, uuid, corrected, num_attempts) values");
        sb.append(generateFormattedValues(usages));
        return sb.toString();
    }

    private String generateBatchUpdateQuery(List<Usage> usages) {
        return generateBatchUpdateQuery(usages, true);
    }

    private String generateBatchUpdateQuery(List<Usage> usages, boolean isUsageUpdate) {
        final StringBuilder sb = new StringBuilder();
        sb.append("REPLACE INTO lb_usage(id, loadbalancer_id, account_id, avg_concurrent_conns, bandwidth_in, bandwidth_out, avg_concurrent_conns_ssl, bandwidth_in_ssl, bandwidth_out_ssl, start_time, end_time, num_polls, num_vips, tags_bitmask, event_type, entry_version, needs_pushed, uuid, corrected, num_attempts) values");
        sb.append(generateFormattedValues(usages, isUsageUpdate));
        return sb.toString();
    }

//    private String generateFormattedValues(List<Usage> usages) {
//        StringBuilder sb = new StringBuilder();
//
//        for (Usage usage : usages) {
//            sb.append("(");
//            if (usage.getId() != null) {
//                sb.append(usage.getId()).append(",");
//            }
//            sb.append(usage.getLoadbalancer().getId()).append(",");
//            sb.append(usage.getAccountId()).append(",");
//            sb.append(usage.getConcurrentConnections()).append(",");
//            sb.append(usage.getIncomingTransfer()).append(",");
//            sb.append(usage.getOutgoingTransfer()).append(",");
//            sb.append(usage.getConcurrentConnectionsSsl()).append(",");
//            sb.append(usage.getIncomingTransferSsl()).append(",");
//            sb.append(usage.getOutgoingTransferSsl()).append(",");
//
//            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            String startTime = formatter.format(usage.getStartTime().getTime());
//            sb.append("'").append(startTime).append("',");
//
//            String endTime = formatter.format(usage.getEndTime().getTime());
//            sb.append("'").append(endTime).append("',");
//
//            sb.append(usage.getNumberOfPolls()).append(",");
//            sb.append(usage.getNumVips()).append(",");
//            sb.append(usage.getTags()).append(",");
//            if (usage.getEventType() == null) {
//                sb.append(usage.getEventType()).append(",");
//            } else {
//                sb.append("'").append(usage.getEventType()).append("'").append(",");
//            }
//
//            //Used for keeping track of updated rows
//            int versionBump;
//            if (usage.getEntryVersion() == null) {
//                //new record
//                versionBump = 0;
//            } else {
//                versionBump = usage.getEntryVersion();
//            }
//            versionBump += 1;
//            sb.append(versionBump);
//            sb.append(",");
//            //Mark as not pushed so job can update the AHUSL
//            sb.append(1);
//            sb.append("),");
//
//        }
//        if (sb.toString().endsWith(",")) {
//            sb.deleteCharAt(sb.lastIndexOf(","));
//        }
//        return sb.toString();
//    }

    private String generateFormattedValues(Collection<Usage> usages) {
        return generateFormattedValues(usages, true);
    }

    private String generateFormattedValues(Collection<Usage> usages, boolean isUpdate) {
        StringBuilder sb = new StringBuilder();

        for (Usage usage : usages) {
            sb.append(generateBaseFormattedValue(usage));
            sb.append(generateUpdatedFormattedValue(usage, isUpdate));
        }
        if (sb.toString().endsWith(",")) {
            sb.deleteCharAt(sb.lastIndexOf(","));
        }
        return sb.toString();
    }

    private String generateBaseFormattedValue(Usage usage) {
        StringBuilder sb = new StringBuilder();

        sb.append("(");
        if (usage.getId() != null) {
            sb.append(usage.getId()).append(",");
        }
        sb.append(usage.getLoadbalancer().getId()).append(",");
        sb.append(usage.getAccountId()).append(",");
        sb.append(usage.getAverageConcurrentConnections()).append(",");
        sb.append(usage.getIncomingTransfer()).append(",");
        sb.append(usage.getOutgoingTransfer()).append(",");
        sb.append(usage.getAverageConcurrentConnectionsSsl()).append(",");
        sb.append(usage.getIncomingTransferSsl()).append(",");
        sb.append(usage.getOutgoingTransferSsl()).append(",");

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTime = formatter.format(usage.getStartTime().getTime());
        sb.append("'").append(startTime).append("',");

        String endTime = formatter.format(usage.getEndTime().getTime());
        sb.append("'").append(endTime).append("',");

        sb.append(usage.getNumberOfPolls()).append(",");
        sb.append(usage.getNumVips()).append(",");
        sb.append(usage.getTags()).append(",");
        if (usage.getEventType() == null) {
            sb.append(usage.getEventType()).append(",");
        } else {
            sb.append("'").append(usage.getEventType()).append("'").append(",");
        }

        return sb.toString();
    }

    private String generateUpdatedFormattedValue(Usage usage, boolean isUsageUpdate) {
        StringBuilder sb = new StringBuilder();
        if (isUsageUpdate) {
            int versionBump;
            if (usage.getEntryVersion() == null) {
                versionBump = 0;
            } else {
                versionBump = usage.getEntryVersion();
            }
            versionBump += 1;
            sb.append(versionBump);
            sb.append(",");
            sb.append(1);
            sb.append(",");
            if (usage.getUuid() == null) {
                sb.append("NULL");
            } else {
                sb.append("'");
                sb.append(usage.getUuid());
                sb.append("'");
            }
            sb.append(",");
            sb.append(usage.isCorrected());
            sb.append(",");
            sb.append(usage.getNumAttempts());
            sb.append("),");
            return sb.toString();
        } else {
            sb.append(usage.getEntryVersion());
            sb.append(",");
            sb.append(usage.isNeedsPushed());
            sb.append(",");
            if (usage.getUuid() == null) {
                sb.append("NULL");
            } else {
                sb.append("'");
                sb.append(usage.getUuid());
                sb.append("'");
            }
            sb.append(",");
            sb.append(usage.isCorrected());
            sb.append(",");
            sb.append(usage.getNumAttempts());
            sb.append("),");
        }
        return sb.toString();
    }

    public List<Integer> getLoadBalancerIdsIn(Collection<Integer> lbIdsToCheckAgainst) {
        if (lbIdsToCheckAgainst == null || lbIdsToCheckAgainst.isEmpty()) return new ArrayList<Integer>();

        Query query = entityManager.createNativeQuery("SELECT id FROM loadbalancer WHERE id in (:loadbalancerIds);")
                .setParameter("loadbalancerIds", lbIdsToCheckAgainst);

        List<Integer> idsInDatabase = (List<Integer>) query.getResultList();
        if (idsInDatabase == null) return new ArrayList<Integer>();

        return idsInDatabase;

    }

    public List<Usage> getRecordForLoadBalancer(Integer loadBalancerId, UsageEvent usageEvent) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Usage> criteria = builder.createQuery(Usage.class);
        Root<Usage> loadBalancerUsageRoot = criteria.from(Usage.class);
        LoadBalancer loadBalancer = new LoadBalancer();
        loadBalancer.setId(loadBalancerId);

        Predicate hasLoadBalancerId = builder.equal(loadBalancerUsageRoot.get(Usage_.loadbalancer), loadBalancer);
        Predicate hasEventType = builder.equal(loadBalancerUsageRoot.get(Usage_.eventType), usageEvent.name());

        criteria.select(loadBalancerUsageRoot);
        criteria.where(builder.and(hasLoadBalancerId, hasEventType));

        return entityManager.createQuery(criteria).getResultList();
    }

    public List<Usage> getUsageRecords(Calendar startTime, Calendar endTime, Integer offset, Integer limit) {
        Query query = entityManager.createNativeQuery("SELECT u.id, u.loadbalancer_id, u.avg_concurrent_conns, u.bandwidth_in, u.bandwidth_out, u.avg_concurrent_conns_ssl, u.bandwidth_in_ssl, u.bandwidth_out_ssl, u.start_time, u.end_time, u.num_polls, u.num_vips, u.tags_bitmask, u.event_type, u.account_id" +
                " FROM lb_usage u WHERE u.start_time between :startTime and :endTime" +
                " and u.end_time between :startTime and :endTime ORDER BY u.account_id, u.loadbalancer_id, u.start_time")
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime);

        final List<Object[]> resultList = query.setFirstResult(offset).setMaxResults(limit + 1).getResultList();
        List<Usage> usages = new ArrayList<Usage>();

        for (Object[] row : resultList) {
            Usage usageItem = rowToUsage(row);
            usages.add(usageItem);
        }

        return usages;
    }

    private Usage rowToUsage(Object[] row) {
        Long startTimeMillis = ((Timestamp) row[8]).getTime();
        Long endTimeMillis = ((Timestamp) row[9]).getTime();
        Calendar startTimeCal = new GregorianCalendar();
        Calendar endTimeCal = new GregorianCalendar();
        startTimeCal.setTimeInMillis(startTimeMillis);
        endTimeCal.setTimeInMillis(endTimeMillis);

        Usage usageItem = new Usage();
        usageItem.setId((Integer) row[0]);
        LoadBalancer lb = new LoadBalancer();
        lb.setId((Integer) row[1]);
        usageItem.setLoadbalancer(lb);
        usageItem.setAverageConcurrentConnections((Double) row[2]);
        usageItem.setIncomingTransfer(((BigInteger) row[3]).longValue());
        usageItem.setOutgoingTransfer(((BigInteger) row[4]).longValue());
        usageItem.setAverageConcurrentConnectionsSsl((Double) row[5]);
        usageItem.setIncomingTransferSsl(((BigInteger) row[6]).longValue());
        usageItem.setOutgoingTransferSsl(((BigInteger) row[7]).longValue());
        usageItem.setStartTime(startTimeCal);
        usageItem.setEndTime(endTimeCal);
        usageItem.setNumberOfPolls((Integer) row[10]);
        usageItem.setNumVips((Integer) row[11]);
        usageItem.setTags((Integer) row[12]);
        usageItem.setEventType((String) row[13]);
        usageItem.setAccountId((Integer) row[14]);
        return usageItem;
    }
}
