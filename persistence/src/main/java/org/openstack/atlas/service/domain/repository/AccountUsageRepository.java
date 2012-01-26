package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.ejb.criteria.OrderImpl;
import org.openstack.atlas.service.domain.entities.AccountUsage;
import org.openstack.atlas.service.domain.entities.AccountUsage_;
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
import java.util.Calendar;
import java.util.List;

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

    public List<AccountUsage> getAccountUsageRecords(Calendar startTime, Calendar endTime, Integer offset, Integer limit) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AccountUsage> criteria = builder.createQuery(AccountUsage.class);
        Root<AccountUsage> lbRoot = criteria.from(AccountUsage.class);

        Predicate betweenDates = builder.between(lbRoot.get(AccountUsage_.startTime), startTime, endTime);

        criteria.select(lbRoot);
        criteria.where(betweenDates);
        criteria.orderBy(new OrderImpl(lbRoot.get(AccountUsage_.accountId), true), new OrderImpl(lbRoot.get(AccountUsage_.startTime), true));
        return entityManager.createQuery(criteria).setFirstResult(offset).setMaxResults(limit + 1).getResultList();
    }
}
