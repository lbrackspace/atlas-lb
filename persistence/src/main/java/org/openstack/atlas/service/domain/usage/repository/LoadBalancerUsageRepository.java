package org.openstack.atlas.service.domain.usage.repository;

import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

@Transactional(value = "usage")
public class LoadBalancerUsageRepository {

    final Log LOG = LogFactory.getLog(LoadBalancerUsageRepository.class);
    @PersistenceContext(unitName = "loadbalancingUsage")
    private EntityManager entityManager;
    private final Integer NUM_DAYS_RETENTION = 120;

    public LoadBalancerUsage getById(Integer id) throws EntityNotFoundException {
        LoadBalancerUsage usageRecord = entityManager.find(LoadBalancerUsage.class, id);
        if (usageRecord == null) {
            String errMsg = String.format("Load balancer usage record not found");
            LOG.warn(errMsg);
            throw new EntityNotFoundException(errMsg);
        }
        return usageRecord;
    }

    public List<LoadBalancerUsage> getMostRecentUsageForLoadBalancers(Collection<Integer> loadBalancerIds) {
        Query query = entityManager.createNativeQuery("SELECT a.* " +
                "FROM lb_usage a, " +
                "(SELECT loadbalancer_id, max(end_time) as end_time FROM lb_usage WHERE loadbalancer_id in (:loadbalancerIds) GROUP BY loadbalancer_id) b " +
                "WHERE a.loadbalancer_id in (:loadbalancerIds) and a.loadbalancer_id = b.loadbalancer_id and a.end_time = b.end_time;", LoadBalancerUsage.class)
                .setParameter("loadbalancerIds", loadBalancerIds);

        List<LoadBalancerUsage> usages = query.getResultList();
        if (usages == null) return new ArrayList<LoadBalancerUsage>();
        return usages;
    }

    public LoadBalancerUsage getMostRecentUsageForLoadBalancer(Integer loadBalancerId) {
        Collection<Integer> loadBalancerIds = new ArrayList<Integer>();
        loadBalancerIds.add(loadBalancerId);
        List<LoadBalancerUsage> usages = getMostRecentUsageForLoadBalancers(loadBalancerIds);
        if (usages == null || usages.isEmpty()) {
            return null;
        }
        return (LoadBalancerUsage) usages.get(0);
    }

    public List<LoadBalancerUsage> getAllRecordsBefore(Calendar time) {
        Query query = entityManager.createQuery("SELECT u FROM LoadBalancerUsage u WHERE u.endTime < :endTime ORDER BY u.endTime ASC")
                .setParameter("endTime", time);

        List<LoadBalancerUsage> usage = (List<LoadBalancerUsage>) query.getResultList();
        if (usage == null) return new ArrayList<LoadBalancerUsage>();
        return usage;
    }

    public void deleteAllRecordsBefore(Calendar time) {
        Query query = entityManager.createQuery("DELETE LoadBalancerUsage u WHERE u.endTime < :timestamp")
                .setParameter("timestamp", time, TemporalType.TIMESTAMP);
        int numRowsDeleted = query.executeUpdate();
        LOG.info(String.format("Deleted %d rows with endTime before %s", numRowsDeleted, time.getTime()));
    }

    public void deleteAllRecordsForLoadBalancer(Integer loadBalancerId) {
        Query query = entityManager.createQuery("DELETE FROM LoadBalancerUsage u WHERE u.loadbalancerId = :loadBalancerId")
                .setParameter("loadBalancerId", loadBalancerId);
        int numRowsDeleted = query.executeUpdate();
        LOG.info(String.format("Deleted %d rows with load balancer id %s", numRowsDeleted, loadBalancerId));
    }

    public void deleteOldRecords() {
        Calendar deletePoint = Calendar.getInstance();
        deletePoint.add(Calendar.DATE, -NUM_DAYS_RETENTION);
        deleteAllRecordsBefore(deletePoint);
    }

    public void batchCreate(List<LoadBalancerUsage> usages) {
        LOG.info(String.format("batchCreate() called with %d records", usages.size()));

        String query = generateBatchInsertQuery(usages);
        entityManager.createNativeQuery(query).executeUpdate();
    }

    public void batchUpdate(List<LoadBalancerUsage> usages) {
        LOG.info(String.format("batchUpdate() called with %d records", usages.size()));

        String query = generateBatchUpdateQuery(usages);
        entityManager.createNativeQuery(query).executeUpdate();
    }

    private String generateBatchInsertQuery(List<LoadBalancerUsage> usages) {
        final StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO lb_usage(account_id, loadbalancer_id, avg_concurrent_conns, cum_bandwidth_bytes_in, cum_bandwidth_bytes_out, last_bandwidth_bytes_in, last_bandwidth_bytes_out, start_time, end_time, num_polls, num_vips, tags_bitmask, event_type) values");
        sb.append(generateFormattedValues(usages));
        return sb.toString();
    }

    private String generateBatchUpdateQuery(List<LoadBalancerUsage> usages) {
        final StringBuilder sb = new StringBuilder();
        sb.append("REPLACE INTO lb_usage(id, account_id, loadbalancer_id, avg_concurrent_conns, cum_bandwidth_bytes_in, cum_bandwidth_bytes_out, last_bandwidth_bytes_in, last_bandwidth_bytes_out, start_time, end_time, num_polls, num_vips, tags_bitmask, event_type) values");
        sb.append(generateFormattedValues(usages));
        return sb.toString();
    }

    private String generateFormattedValues(List<LoadBalancerUsage> usages) {
        StringBuilder sb = new StringBuilder();

        for (LoadBalancerUsage usage : usages) {
            sb.append("(");
            if (usage.getId() != null) {
                sb.append(usage.getId()).append(",");
            }
            sb.append(usage.getAccountId()).append(",");
            sb.append(usage.getLoadbalancerId()).append(",");
            sb.append(usage.getAverageConcurrentConnections()).append(",");
            sb.append(usage.getCumulativeBandwidthBytesIn()).append(",");
            sb.append(usage.getCumulativeBandwidthBytesOut()).append(",");
            sb.append(usage.getLastBandwidthBytesIn()).append(",");
            sb.append(usage.getLastBandwidthBytesOut()).append(",");

            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String startTime = formatter.format(usage.getStartTime().getTime());
            sb.append("'").append(startTime).append("',");

            String endTime = formatter.format(usage.getEndTime().getTime());
            sb.append("'").append(endTime).append("',");

            sb.append(usage.getNumberOfPolls()).append(",");
            sb.append(usage.getNumVips()).append(",");
            sb.append(usage.getTags()).append(",");
            if(usage.getEventType() == null) {
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
