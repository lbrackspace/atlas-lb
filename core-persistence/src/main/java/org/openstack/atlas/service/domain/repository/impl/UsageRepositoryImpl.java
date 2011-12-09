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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Repository
@Transactional
public class UsageRepositoryImpl implements UsageRepository {
    final Log LOG = LogFactory.getLog(UsageRepositoryImpl.class);
    @PersistenceContext(unitName = "loadbalancing")
    protected EntityManager entityManager;

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
    public List<UsageRecord> getMostRecentUsageRecordsForLoadBalancers(Collection<Integer> lbIds) {
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
    public UsageRecord getMostRecentUsageForLoadBalancer(Integer loadBalancerId) {
        List<Integer> loadBalancerIds = new ArrayList<Integer>();
        loadBalancerIds.add(loadBalancerId);
        List<UsageRecord> usages = getMostRecentUsageRecordsForLoadBalancers(loadBalancerIds);
        if (usages == null || usages.isEmpty()) {
            return null;
        }
        return usages.get(0);
    }

    @Override
    public void batchCreate(List<UsageRecord> recordsToInsert) {
        LOG.debug(String.format("batchCreate() called with %d records", recordsToInsert.size()));
        String query = generateBatchInsertQuery(recordsToInsert);
        entityManager.createNativeQuery(query).executeUpdate();
    }

    @Override
    public void batchUpdate(List<UsageRecord> recordsToUpdate) {
        LOG.debug(String.format("batchUpdate() called with %d records", recordsToUpdate.size()));
        String query = generateBatchUpdateQuery(recordsToUpdate);
        entityManager.createNativeQuery(query).executeUpdate();
    }

    protected String generateBatchInsertQuery(List<UsageRecord> usages) {
        final StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO load_balancer_usage(vendor, load_balancer_id, event, transfer_bytes_in, transfer_bytes_out, last_bytes_in_count, last_bytes_out_count, start_time, end_time) values");
        sb.append(generateFormattedValues(usages));
        return sb.toString();
    }

    protected String generateBatchUpdateQuery(List<UsageRecord> usages) {
        final StringBuilder sb = new StringBuilder();
        sb.append("REPLACE INTO load_balancer_usage(vendor, id, load_balancer_id, event, transfer_bytes_in, transfer_bytes_out, last_bytes_in_count, last_bytes_out_count, start_time, end_time) values");
        sb.append(generateFormattedValues(usages));
        return sb.toString();
    }

    protected String generateFormattedValues(List<UsageRecord> usages) {
        StringBuilder sb = new StringBuilder();

        for (UsageRecord usage : usages) {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String startTime = formatter.format(usage.getStartTime().getTime());
            String endTime = formatter.format(usage.getEndTime().getTime());

            sb.append("(");
            sb.append("'CORE'").append(",");
            if (usage.getId() != null) sb.append(usage.getId()).append(",");
            sb.append(usage.getLoadBalancer().getId()).append(",");
            if (usage.getEvent() != null) sb.append("'").append(usage.getEvent()).append("',");
            else sb.append(usage.getEvent()).append(",");
            sb.append(usage.getTransferBytesIn()).append(",");
            sb.append(usage.getTransferBytesOut()).append(",");
            sb.append(usage.getLastBytesInCount()).append(",");
            sb.append(usage.getLastBytesOutCount()).append(",");
            sb.append("'").append(startTime).append("',");
            sb.append("'").append(endTime).append("'");
            sb.append("),");

        }
        if (sb.toString().endsWith(",")) {
            sb.deleteCharAt(sb.lastIndexOf(","));
        }
        return sb.toString();
    }
}
