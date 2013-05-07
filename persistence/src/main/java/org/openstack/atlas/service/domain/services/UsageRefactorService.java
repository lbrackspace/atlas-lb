package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;


public interface UsageRefactorService {
    public void createUsageEvent(LoadBalancerHostUsage loadBalancerHostUsageEvent);

    public LoadBalancerHostUsage getLastRecordForLbIdAndHostId(int lbId, int hostId);

    //Keys loadbalancerId, hostId
    public Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> getAllLoadBalancerHostUsages();

    public void batchCreateLoadBalancerHostUsages(List<LoadBalancerHostUsage> usages);

    public void deleteOldLoadBalancerHostUsages(Calendar deleteTimeMarker);

    public void batchCreateLoadBalancerMergedHostUsages(List<LoadBalancerMergedHostUsage> usages);

    public void batchDeleteLoadBalancerMergedHostUsages(Collection<LoadBalancerMergedHostUsage> usages);

}
