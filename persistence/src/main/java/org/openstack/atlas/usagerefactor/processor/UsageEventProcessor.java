package org.openstack.atlas.usagerefactor.processor;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.usagerefactor.SnmpUsage;

import java.util.Calendar;
import java.util.List;

public interface UsageEventProcessor {
    void processUsageEvent(List<SnmpUsage> usages, LoadBalancer loadBalancer, UsageEvent usageEvent);

    public LoadBalancerMergedHostUsage mapSnmpUsage(SnmpUsage usage, LoadBalancer loadBalancer, Calendar pollTime, UsageEvent usageEvent);

}
