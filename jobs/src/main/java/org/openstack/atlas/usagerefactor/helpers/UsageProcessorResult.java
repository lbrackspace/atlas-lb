package org.openstack.atlas.usagerefactor.helpers;

import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;

import java.util.List;

public class UsageProcessorResult {

    private List<LoadBalancerMergedHostUsage> mergedUsages;
    private List<LoadBalancerHostUsage> lbHostUsages;

        public List<LoadBalancerMergedHostUsage> getMergedUsages() {
        return mergedUsages;
    }

    public List<LoadBalancerHostUsage> getLbHostUsages() {
        return lbHostUsages;
    }

    public UsageProcessorResult(List<LoadBalancerMergedHostUsage> mergedUsages, List<LoadBalancerHostUsage> lbHostUsages) {
        this.mergedUsages = mergedUsages;
        this.lbHostUsages = lbHostUsages;
    }
}
