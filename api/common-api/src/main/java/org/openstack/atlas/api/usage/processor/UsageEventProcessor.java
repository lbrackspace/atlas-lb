package org.openstack.atlas.api.usage.processor;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.usagerefactor.SnmpUsage;

public interface UsageEventProcessor {
    void processUsageEvent(SnmpUsage usage, LoadBalancer loadBalancer, UsageEvent usageEvent);
}
