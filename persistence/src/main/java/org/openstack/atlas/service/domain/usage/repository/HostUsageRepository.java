package org.openstack.atlas.service.domain.usage.repository;

import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.usage.entities.HostUsage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.util.Calendar;
import java.util.List;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.RestApiConfiguration;

@Repository
@Transactional(value = "usage")
public class HostUsageRepository {
    final Log LOG = LogFactory.getLog(HostUsageRepository.class);
    @PersistenceContext(unitName = "loadbalancingUsage")
    private EntityManager entityManager;
    private final Integer NUM_DAYS_RETENTION = 60;
    private final Integer DEFAULT_DELETE_LIMIT = 10000;

    public HostUsage getById(Integer id) throws EntityNotFoundException {
        HostUsage hostUsageRecord = entityManager.find(HostUsage.class, id);
        if (hostUsageRecord == null) {
            String errMsg = String.format("Host usage record not found");
            LOG.warn(errMsg);
            throw new EntityNotFoundException(errMsg);
        }
        return hostUsageRecord;
    }

    public List<HostUsage> getByDateRange(Calendar startTime, Calendar endTime) {
        Query query = entityManager.createQuery("FROM HostUsage u WHERE u.snapshotTime between :startTime and :endTime order by u.hostId, u.snapshotTime asc")
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime);

        return query.getResultList();
    }

    public void save(HostUsage usageToSave) {
        entityManager.persist(usageToSave);
    }

    public void deleteOldRecords() {
        Calendar deletePoint = Calendar.getInstance();
        deletePoint.add(Calendar.DATE, -NUM_DAYS_RETENTION);
        deleteAllRecordsBefore(deletePoint);
    }

    private void deleteAllRecordsBefore(Calendar time) {
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
            Query nativeQ = entityManager.createNativeQuery("DELETE FROM host_usage WHERE snapshot_time <= :timestamp LIMIT :limit")
                    .setParameter("timestamp", time, TemporalType.TIMESTAMP).setParameter("limit", limitInt);
            numRowsDeleted = nativeQ.executeUpdate();
            totalRowsDeleted += numRowsDeleted;
            batchCount++;
            LOG.info(String.format("Deleted %d rows with endTime before %s from 'host_usage' table in batch %d.", numRowsDeleted, time.getTime(), batchCount));
        } while(numRowsDeleted > 0);

        LOG.info(String.format("Finished deleting rows. Deleted %d total rows in %d batch(es) with endTime before %s from 'host_usage' table.", totalRowsDeleted, batchCount, time.getTime()));
    }
}
