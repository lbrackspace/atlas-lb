package org.openstack.atlas.service.domain.usage.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional(value = "lb_host_usage")
public class HostUsageRefactorRepository {
    final Log LOG = LogFactory.getLog(HostUsageRefactorRepository.class);

    @PersistenceContext(unitName = "loadbalancingUsage")
    private EntityManager entityManager;

    public void create(LoadBalancerHostUsage usageRecord) {
//        entityManager.persist(usageEventRecord);
    }

    public void batchCreate(List<LoadBalancerHostUsage> usageRecords) {

    }

    public void getByLbId(int lbId) {
//        entityManager.persist(usageEventRecord);
    }

    public LoadBalancerHostUsage getMostRecentUsageRecordForLbId(int lbId) {
//        entityManager.persist(usageEventRecord);
        return new LoadBalancerHostUsage();
    }

    public void deleteOldHostUsage(Calendar deleteTimeMarker) {

    }

    public Map<Integer, List<LoadBalancerHostUsage>> getLoadBalancerHostUsageRecords() {
        return new HashMap<Integer, List<LoadBalancerHostUsage>>();
    }

}
