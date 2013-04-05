package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;

import java.util.List;
import java.util.Map;

public interface UsagePoller {

    public void processRecords();
    public List<LoadBalancerHostUsage> getLoadBalancerHostUsageRecords();
    public Map<Integer, SnmpUsage> getCurrentData() throws Exception;
    public void deleteLoadBalancerHostUsageRecords(int markerId);
    public void insertLoadBalancerUsagePerHost(List<LoadBalancerHostUsage> lbHostUsages);
    public void insertMergedRecords(List<LoadBalancerMergedHostUsage> mergedRecords);

}
