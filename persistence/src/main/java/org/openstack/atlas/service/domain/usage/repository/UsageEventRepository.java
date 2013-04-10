package org.openstack.atlas.service.domain.usage.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Transactional(value = "lb_host_usage")
public class UsageEventRepository {
    final Log LOG = LogFactory.getLog(UsageEventRepository.class);

    @PersistenceContext(unitName = "loadbalancingUsage")
    private EntityManager entityManager;

    public void create(LoadBalancerHostUsage usageEventRecord) {
//        entityManager.persist(usageEventRecord);
    }
}
