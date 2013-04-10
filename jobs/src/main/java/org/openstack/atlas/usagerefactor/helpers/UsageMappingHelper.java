package org.openstack.atlas.usagerefactor.helpers;

import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.usagerefactor.SnmpUsage;

import java.util.HashMap;
import java.util.Map;

public class UsageMappingHelper {

    public static Map<Integer, Map<Integer, SnmpUsage>> swapKeyGrouping(
                  Map<Integer, Map<Integer, SnmpUsage>> groupedByHosts) {
        Map<Integer, Map<Integer, SnmpUsage>> groupedByLoadBalancers = new HashMap<Integer, Map<Integer, SnmpUsage>>();
        for (Integer hostId : groupedByHosts.keySet()) {
            for (Integer loadBalancerId : groupedByHosts.get(hostId).keySet()) {
                Map<Integer, SnmpUsage> hostMap;
                if (!groupedByLoadBalancers.containsKey(loadBalancerId)) {
                    hostMap = new HashMap<Integer, SnmpUsage>();
                } else {
                    hostMap = groupedByLoadBalancers.get(loadBalancerId);
                }
                hostMap.put(hostId, groupedByHosts.get(hostId).get(loadBalancerId));
                groupedByLoadBalancers.put(loadBalancerId, hostMap);
            }
        }
        return groupedByLoadBalancers;
    }
}
