package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatusHistory;
import org.openstack.atlas.service.domain.entities.UserPages;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.util.Constants;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Repository
@Transactional
public class LoadBalancerStatusHistoryRepository {

    final Log LOG = LogFactory.getLog(LoadBalancerStatusHistoryRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;
    private final Integer PAGE_SIZE = 10;
    private final int MAX_SERVICE_EVENT = 20;

    public LoadBalancerStatusHistoryRepository() {
    }

    public LoadBalancerStatusHistoryRepository(EntityManager em) {
        this.entityManager = em;
    }

    public LoadBalancerStatusHistory save(LoadBalancerStatusHistory loadBalancerStatusHistory) {
        entityManager.persist(loadBalancerStatusHistory);
        return loadBalancerStatusHistory;
    }

    public List<LoadBalancerStatusHistory> getStateHistoryForAccount(int accountId, int offset, int limit, Integer marker) {
        List<LoadBalancerStatusHistory> lbshlist = new ArrayList<LoadBalancerStatusHistory>();
        String qStr = "FROM LoadBalancerStatusHistory u where u.accountId = :aid";
        if(marker != null) {
            qStr = qStr+" and u.id >= :marker";
        }
        Query q = entityManager.createQuery(qStr).setParameter("aid", accountId);
        if(marker != null) {
            q.setParameter("marker", marker);
        }
        q.setFirstResult(offset).setMaxResults(limit+1);
        lbshlist = q.getResultList();
        if (lbshlist.size() <= 0) {
            return new ArrayList<LoadBalancerStatusHistory>();
        } else {
            return lbshlist;
        }
    }

    public List<LoadBalancerStatusHistory> getLBStatusHistoryOlderThanSixMonths(Calendar cal) {

        List<LoadBalancerStatusHistory> lbshlist;
        lbshlist = entityManager.createQuery("Select e FROM LoadBalancerStatusHistory e WHERE e.created <= :days").setParameter("days", cal).getResultList();
        return  lbshlist;
    }

    public void batchDeleteStatusHistoryForLBOlderThanSixMonths(Calendar cal) {
        List<LoadBalancerStatusHistory> lbshList;
        lbshList = getLBStatusHistoryOlderThanSixMonths(cal);
        List<LoadBalancerStatusHistory> lbshBatch = new ArrayList<>();
        List<Integer> lbshIds = new ArrayList<>();

        for ( int i = 1; i <= lbshList.size(); i++ ) {
            lbshBatch.add(lbshList.get(i-1));
            if (i % 100 == 0 || i == lbshList.size()) {
                for (LoadBalancerStatusHistory loadBalancerStatusHistory : lbshBatch){
                    lbshIds.add(loadBalancerStatusHistory.getId());
                }
                entityManager.createQuery("DELETE LoadBalancerStatusHistory e WHERE e.id in (:ids)")
                        .setParameter("ids", lbshIds)
                        .executeUpdate();
                lbshIds.clear();
            }
            lbshBatch.clear();
        }
    }
}
