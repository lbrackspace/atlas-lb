package org.openstack.atlas.service.domain.usage.repository;

import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.usage.entities.HostUsage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.util.Calendar;
import java.util.List;

@Transactional(value = "usage")
public class HostUsageRepository {
    final Log LOG = LogFactory.getLog(HostUsageRepository.class);
    @PersistenceContext(unitName = "loadbalancingUsage")
    private EntityManager entityManager;
    private final Integer NUM_DAYS_RETENTION = 60;

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
        Query query = entityManager.createQuery("DELETE HostUsage u WHERE u.snapshotTime < :timestamp")
                .setParameter("timestamp", time, TemporalType.TIMESTAMP);
        int numRowsDeleted = query.executeUpdate();
        LOG.info(String.format("Deleted %d rows with endTime before %s from 'host_usage' table.", numRowsDeleted, time.getTime()));
    }
}
