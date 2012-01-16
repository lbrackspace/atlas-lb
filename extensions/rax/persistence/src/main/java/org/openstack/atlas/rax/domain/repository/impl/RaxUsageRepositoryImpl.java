package org.openstack.atlas.rax.domain.repository.impl;

import org.openstack.atlas.rax.domain.entity.RaxUsageRecord;
import org.openstack.atlas.service.domain.entity.UsageRecord;
import org.openstack.atlas.service.domain.repository.impl.UsageRepositoryImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Primary
@Repository
@Transactional
public class RaxUsageRepositoryImpl extends UsageRepositoryImpl {

    @Override
    public List<UsageRecord> getMostRecentUsageRecordsForLoadBalancers(Collection<Integer> lbIds) {
        if (lbIds == null || lbIds.isEmpty()) return new ArrayList<UsageRecord>();

        Query query = entityManager.createNativeQuery("SELECT a.* " +
                "FROM load_balancer_usage a, " +
                "(SELECT load_balancer_id, max(end_time) as end_time FROM load_balancer_usage WHERE load_balancer_id in (:lbIds) GROUP BY load_balancer_id) b " +
                "WHERE a.load_balancer_id in (:lbIds) and a.load_balancer_id = b.load_balancer_id and a.end_time = b.end_time;", RaxUsageRecord.class)
                .setParameter("lbIds", lbIds);

        List<UsageRecord> usage = (List<UsageRecord>) query.getResultList();
        if (usage == null) return new ArrayList<UsageRecord>();

        return usage;
    }

    @Override
    protected String generateBatchInsertQuery(List<UsageRecord> usages) {
        final StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO load_balancer_usage(vendor, load_balancer_id, event, transfer_bytes_in, transfer_bytes_out, last_bytes_in_count, last_bytes_out_count, avg_concurrent_conns, num_polls, start_time, end_time) values");
        sb.append(generateFormattedValues(usages));
        return sb.toString();
    }

    @Override
    protected String generateBatchUpdateQuery(List<UsageRecord> usages) {
        final StringBuilder sb = new StringBuilder();
        sb.append("REPLACE INTO load_balancer_usage(vendor, id, load_balancer_id, event, transfer_bytes_in, transfer_bytes_out, last_bytes_in_count, last_bytes_out_count, avg_concurrent_conns, num_polls, start_time, end_time) values");
        sb.append(generateFormattedValues(usages));
        return sb.toString();
    }

    @Override
    protected String generateFormattedValues(List<UsageRecord> usages) {
        StringBuilder sb = new StringBuilder();

        for (UsageRecord usage : usages) {
            final RaxUsageRecord raxUsageRecord;

            if (usage instanceof RaxUsageRecord) {
                raxUsageRecord = (RaxUsageRecord) usage;
            } else {
                raxUsageRecord = new RaxUsageRecord(usage);
            }

            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String startTime = formatter.format(raxUsageRecord.getStartTime().getTime());
            String endTime = formatter.format(raxUsageRecord.getEndTime().getTime());

            sb.append("(");
            sb.append("'RAX'").append(",");
            if (raxUsageRecord.getId() != null) sb.append(raxUsageRecord.getId()).append(",");
            sb.append(raxUsageRecord.getLoadBalancer().getId()).append(",");
            if (raxUsageRecord.getEvent() != null) sb.append("'").append(raxUsageRecord.getEvent()).append("',");
            else sb.append(raxUsageRecord.getEvent()).append(",");
            sb.append(raxUsageRecord.getTransferBytesIn()).append(",");
            sb.append(raxUsageRecord.getTransferBytesOut()).append(",");
            sb.append(raxUsageRecord.getLastBytesInCount()).append(",");
            sb.append(raxUsageRecord.getLastBytesOutCount()).append(",");
            sb.append(raxUsageRecord.getAverageConcurrentConnections()).append(",");
            sb.append(raxUsageRecord.getNumberOfPolls()).append(",");
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
