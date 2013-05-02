package org.openstack.atlas.service.domain.usage.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsageEvent;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Repository
@Transactional(value = "usage")
public class HostUsageRefactorRepository {
    final Log LOG = LogFactory.getLog(HostUsageRefactorRepository.class);

    @PersistenceContext(unitName = "loadbalancingUsage")
    private EntityManager entityManager;

    public void create(LoadBalancerHostUsage usageRecord) {
        entityManager.persist(usageRecord);
    }

    // TODO:  Logic needs tested in the requests
    public void batchCreate(List<LoadBalancerHostUsage> usageRecords) {
        LOG.info(String.format("batchCreate() called with %d records", usageRecords.size()));
        String query = generateBatchInsertQuery(usageRecords);
        entityManager.createNativeQuery(query).executeUpdate();
    }

    private String generateBatchInsertQuery(List<LoadBalancerHostUsage> usages) {
        final StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO lb_usage_event values");
        sb.append(generateFormattedValuesForList(usages));
        return sb.toString();
    }

    private String generateFormattedValuesForList(List<LoadBalancerHostUsage> usages) {
        String queryString = "";
        for (LoadBalancerHostUsage usage : usages) {
            queryString += generateFormattedValues(usage);
            queryString += "),";
        }
        if (queryString.endsWith(",")) {
            queryString = queryString.substring(0, queryString.length() - 1);
        }
        return queryString;
    }

    private String generateFormattedValues(LoadBalancerHostUsage usage) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(usage.getAccountId()).append(",");
        sb.append(usage.getLoadbalancerId()).append(",");
        sb.append(usage.getHostId()).append(",");
        sb.append(usage.getIncomingTransfer()).append(",");
        sb.append(usage.getIncomingTransferSsl()).append(",");
        sb.append(usage.getOutgoingTransfer()).append(",");
        sb.append(usage.getOutgoingTransferSsl()).append(",");
        sb.append(usage.getConcurrentConnections()).append(",");
        sb.append(usage.getConcurrentConnectionsSsl()).append(",");
        sb.append(usage.getTagsBitmask()).append(",");

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTime = formatter.format(usage.getPollTime().getTime());
        sb.append("'").append(startTime).append("',");

        sb.append(usage.getNumVips()).append(",");
        if (usage.getEventType() == null) {
            sb.append(usage.getEventType());
        } else {
            sb.append("'").append(usage.getEventType()).append("'");
        }
        sb.append(")");

        return sb.toString();
    }

    public void getByLbId(int lbId) {
//        entityManager.persist(usageEventRecord);
    }

    public LoadBalancerHostUsage getMostRecentUsageRecordForLbId(int lbId) {
//        entityManager.persist(usageEventRecord);
        return new LoadBalancerHostUsage();
    }

    public void deleteOldHostUsage(Calendar deleteTimeMarker) {
        Query query = entityManager.createQuery("DELETE LoadBalancerHostUsage u WHERE u.snapshotTime < :deleteTimeMarker")
                .setParameter("deleteTimeMarker", deleteTimeMarker, TemporalType.TIMESTAMP);
        int numRowsDeleted = query.executeUpdate();
        LOG.info(String.format("Deleted %d rows with endTime before %s from 'lb_host_usage' table.",
                numRowsDeleted, deleteTimeMarker.getTime()));
    }

    public List<LoadBalancerHostUsage> getAllLoadBalancerHostUsageRecords() {
        String queryStr = "from LoadBalancerHostUsage";
        List<LoadBalancerHostUsage> hosts;
        hosts = entityManager.createQuery(queryStr).getResultList();
        return hosts;
    }

}
