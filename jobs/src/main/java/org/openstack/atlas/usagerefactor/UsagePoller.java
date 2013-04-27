package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;

import java.util.List;
import java.util.Map;

public interface UsagePoller {

    public List<LoadBalancerMergedHostUsage> processRecords();

}
