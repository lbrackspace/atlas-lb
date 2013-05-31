package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsageEvent;

import java.util.ArrayList;
import java.util.List;

public class MigrationProcessor {

    public List<LoadBalancerMergedHostUsage> process(List<LoadBalancerUsage> loadBalancerUsages, List<LoadBalancerUsageEvent> loadBalancerUsageEvents, List<LoadBalancerHostUsage> loadBalancerHostUsages) {
        List<LoadBalancerMergedHostUsage> loadBalancerMergedHostUsages = new ArrayList<LoadBalancerMergedHostUsage>();

        loadBalancerUsageEvents = removeDuplicateEvents(loadBalancerUsageEvents, loadBalancerMergedHostUsages);

        return loadBalancerMergedHostUsages;
    }

    // TODO: Test
    protected List<LoadBalancerUsageEvent> removeDuplicateEvents(List<LoadBalancerUsageEvent> loadBalancerUsageEvents, List<LoadBalancerMergedHostUsage> loadBalancerMergedHostUsages) {
        for (LoadBalancerMergedHostUsage loadBalancerMergedHostUsage : loadBalancerMergedHostUsages) {
            for (LoadBalancerUsageEvent loadBalancerUsageEvent : loadBalancerUsageEvents) {
                if (loadBalancerMergedHostUsage.getLoadbalancerId() == loadBalancerUsageEvent.getAccountId()
                        && loadBalancerMergedHostUsage.getPollTime().equals(loadBalancerUsageEvent.getStartTime())
                        && loadBalancerMergedHostUsage.getEventType().name().equals(loadBalancerUsageEvent.getEventType())) {
                    System.out.println(String.format("Removing duplicate usage event for loadbalancer %d...", loadBalancerUsageEvent.getLoadbalancerId()));
                    loadBalancerUsageEvents.remove(loadBalancerUsageEvent);
                    break;
                }
            }
        }

        return loadBalancerUsageEvents;
    }
}