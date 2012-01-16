package org.openstack.atlas.service.domain.repository.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entity.UsageEventRecord;
import org.openstack.atlas.service.domain.repository.UsageEventRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional
public class UsageEventRepositoryImpl implements UsageEventRepository {
    final Log LOG = LogFactory.getLog(UsageEventRepositoryImpl.class);
    @PersistenceContext(unitName = "loadbalancing")
    protected EntityManager entityManager;

    @Override
    public List<UsageEventRecord> getAllUsageEventEntries() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UsageEventRecord> criteria = builder.createQuery(UsageEventRecord.class);
        Root<UsageEventRecord> usageEventRecordRoot = criteria.from(UsageEventRecord.class);

        criteria.select(usageEventRecordRoot);
        return entityManager.createQuery(criteria).getResultList();
    }

    @Override
    public void batchCreate(List<UsageEventRecord> usages) {
        LOG.info(String.format("batchCreate() called with %d records", usages.size()));
        String query = generateBatchInsertQuery(usages);
        entityManager.createNativeQuery(query).executeUpdate();
    }

    @Override
    public void batchDelete(List<UsageEventRecord> usages) {
        List<Integer> usageIds = new ArrayList<Integer>();

        for (UsageEventRecord usage : usages) {
            usageIds.add(usage.getId());
        }

        entityManager.createQuery("DELETE UsageEventRecord e WHERE e.id in (:ids)")
                .setParameter("ids", usageIds)
                .executeUpdate();
    }

    private String generateBatchInsertQuery(List<UsageEventRecord> usages) {
        final StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO load_balancer_usage_event(vendor, load_balancer_id, event, start_time) values");
        sb.append(generateFormattedValues(usages));
        return sb.toString();
    }

    private String generateFormattedValues(List<UsageEventRecord> usages) {
        StringBuilder sb = new StringBuilder();

        for (UsageEventRecord usage : usages) {
            sb.append("(");
            if (usage.getId() != null) {
                sb.append(usage.getId()).append(",");
            }
            sb.append("'CORE'").append(",");
            sb.append(usage.getLoadBalancer().getId()).append(",");
            sb.append("'").append(usage.getEvent()).append("',");

            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String startTime = formatter.format(usage.getStartTime().getTime());
            sb.append("'").append(startTime).append("'");
            sb.append("),");
        }
        if (sb.toString().endsWith(",")) {
            sb.deleteCharAt(sb.lastIndexOf(","));
        }
        return sb.toString();
    }
}
