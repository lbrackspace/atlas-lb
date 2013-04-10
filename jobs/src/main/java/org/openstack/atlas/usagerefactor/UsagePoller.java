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
    public List<LoadBalancerHostUsage> getLoadBalancerHostUsageRecords();
    public Map<Integer, Map<Integer, SnmpUsage>> getCurrentData() throws Exception;
    public void deleteLoadBalancerHostUsageRecords(Calendar deleteTimeMarker);
    public void insertLoadBalancerUsagePerHost(List<LoadBalancerHostUsage> lbHostUsages);
    public void insertMergedRecords(List<LoadBalancerMergedHostUsage> mergedRecords);

}
