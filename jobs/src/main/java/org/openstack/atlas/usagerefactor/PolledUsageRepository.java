package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;

import java.util.List;

public interface PolledUsageRepository {
    List<LoadBalancerMergedHostUsage> getAllRecords(List<Integer> loadbalancerIds);
}
