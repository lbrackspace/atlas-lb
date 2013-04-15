package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.usagerefactor.helpers.HostIdLoadbalancerIdKey;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public interface UsagePoller {

    public void processRecords();

    /*
     * Key of first Map is hostId
     * Key of nested map is loadBalancerId
     */
    //Key should loadbalancerId
    public Map<Integer, List<LoadBalancerHostUsage>> getLoadBalancerHostUsageRecords();
    //Parent key should be hostId, child key should be loadbalancerId
    public Map<Integer, Map<Integer, SnmpUsage>> getCurrentData() throws Exception;
    public void deleteLoadBalancerHostUsageRecords(Calendar deleteTimeMarker);
    public void insertLoadBalancerUsagePerHost(List<LoadBalancerHostUsage> lbHostUsages);
    public void insertMergedRecords(List<LoadBalancerMergedHostUsage> mergedRecords);
}
