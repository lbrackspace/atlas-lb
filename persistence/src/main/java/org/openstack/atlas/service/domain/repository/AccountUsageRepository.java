package org.openstack.atlas.service.domain.repository;

import org.openstack.atlas.service.domain.entities.AccountUsage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.util.Calendar;

@Repository
@Transactional
public class AccountUsageRepository {

    final Log LOG = LogFactory.getLog(AccountUsageRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;
    private final Integer NUM_DAYS_RETENTION = 120;

    public void save(AccountUsage accountUsage) {
        entityManager.persist(accountUsage);
    }

    public void deleteAllRecordsBefore(Calendar time) {
        Query query = entityManager.createQuery("DELETE AccountUsage u WHERE u.endTime < :timestamp")
                .setParameter("timestamp", time, TemporalType.TIMESTAMP);
        int numRowsDeleted = query.executeUpdate();
        LOG.info(String.format("Deleted %d rows with endTime before %s", numRowsDeleted, time.getTime()));
    }

    public void deleteOldRecords() {
        Calendar deletePoint = Calendar.getInstance();
        deletePoint.add(Calendar.DATE, -NUM_DAYS_RETENTION);
        deleteAllRecordsBefore(deletePoint);
    }
}
