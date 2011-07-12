package org.openstack.atlas.service.domain.repository;

import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Repository
@Transactional
public class RateLimitRepository {

    final Log LOG = LogFactory.getLog(RateLimitRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    public List<Integer> getAllRateLimitedLoadBalancerIds() throws EntityNotFoundException {
        List<Integer> loadBalancerIds = entityManager.createQuery("SELECT r.loadbalancer.id FROM RateLimit r").getResultList();
        if (loadBalancerIds != null && loadBalancerIds.size() > 0) {
            return loadBalancerIds;
        } else {
            return new ArrayList<Integer>();
        }
    }
}