package org.openstack.atlas.usagerefactor.processor;

import org.openstack.atlas.service.domain.entities.AccountUsage;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.usagerefactor.SnmpUsage;

import java.util.Calendar;
import java.util.List;

public interface UsageEventProcessor {
    public void processUsageEvent(List<SnmpUsage> usages, LoadBalancer loadBalancer, UsageEvent usageEvent, Calendar pollTime);

    public AccountUsage createAccountUsageEntry(LoadBalancer loadBalancer, Calendar eventTime);
}
