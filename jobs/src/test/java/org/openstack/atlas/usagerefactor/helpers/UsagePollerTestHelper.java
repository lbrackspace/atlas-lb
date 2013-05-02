package org.openstack.atlas.usagerefactor.helpers;

import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsagePollerTestHelper {
    public static Map<Integer, List<LoadBalancerHostUsage>> groupLBHostUsagesByLoadBalancer(
            List<LoadBalancerHostUsage> lbHostUsages)
    {
        Map<Integer, List<LoadBalancerHostUsage>> lbMap = new HashMap<Integer, List<LoadBalancerHostUsage>>();
        for (LoadBalancerHostUsage lbHostUsage : lbHostUsages) {
            if (!lbMap.containsKey(lbHostUsage.getLoadbalancerId())){
                lbMap.put(lbHostUsage.getLoadbalancerId(), new ArrayList<LoadBalancerHostUsage>());
            }
            lbMap.get(lbHostUsage.getLoadbalancerId()).add(lbHostUsage);
        }
        return lbMap;
    }
}
