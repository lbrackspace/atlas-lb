package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;


public interface UsageRefactorService {
    public void createUsageEvent(LoadBalancerHostUsage loadBalancerHostUsageEvent);

    public LoadBalancerHostUsage getRecentHostUsageRecord(int lbId);

}
