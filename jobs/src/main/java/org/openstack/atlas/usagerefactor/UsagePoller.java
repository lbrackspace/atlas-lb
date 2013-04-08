package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;

import java.util.List;
import java.util.Map;

public interface UsagePoller {

    public void processRecords();

    /*
     * Key of first Map is hostId
     * Key of nested map is loadBalancerId
     */
    public Map<Integer, Map<Integer, LoadBalancerHostUsage>> getLoadBalancerHostUsageRecords();
    public Map<Integer, Map<Integer, SnmpUsage>> getCurrentData() throws Exception;
    public void deleteLoadBalancerHostUsageRecords(int markerId);
    public void insertLoadBalancerUsagePerHost(List<LoadBalancerHostUsage> lbHostUsages);
    public void insertMergedRecords(List<LoadBalancerMergedHostUsage> mergedRecords);

}
