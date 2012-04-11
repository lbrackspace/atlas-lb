package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatusHistory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
}
