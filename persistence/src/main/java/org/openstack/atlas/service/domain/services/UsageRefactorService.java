package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;

import java.util.List;
import java.util.Map;


public interface UsageRefactorService {
    public void createUsageEvent(LoadBalancerHostUsage loadBalancerHostUsageEvent);

    public LoadBalancerHostUsage getRecentHostUsageRecord(int lbId);

    public Map<Integer, List<LoadBalancerHostUsage>> getAllLoadBalancerHostUsages();

}
