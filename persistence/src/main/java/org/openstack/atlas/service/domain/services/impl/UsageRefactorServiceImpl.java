package org.openstack.atlas.service.domain.services.impl;

import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsageRefactorServiceImpl extends BaseService implements UsageRefactorService {

    @Override
    public void createUsageEvent(LoadBalancerHostUsage loadBalancerHostUsage) {
        hostUsageRefactorRepository.create(loadBalancerHostUsage);
    }

    @Override
    public LoadBalancerHostUsage getRecentHostUsageRecord(int lbId) {
        return hostUsageRefactorRepository.getMostRecentUsageRecordForLbId(lbId);
    }

    @Override
    public Map<Integer, List<LoadBalancerHostUsage>> getAllLoadBalancerHostUsages() {
        List<LoadBalancerHostUsage> lbHostUsages = hostUsageRefactorRepository.getAllLoadBalancerHostUsageRecords();
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
