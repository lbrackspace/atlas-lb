package org.openstack.atlas.service.domain.services.impl;

import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.exceptions.DeletedStatusException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.UsageService;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsageEvent;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;

@Service
public class UsageServiceImpl extends BaseService implements UsageService {

    @Override
    public List<Usage> getUsageByAccountIdandLbId(Integer accountId, Integer loadBalancerId, Calendar startTime, Calendar endTime) throws EntityNotFoundException, DeletedStatusException {
        return loadBalancerRepository.getUsageByAccountIdandLbId(accountId, loadBalancerId, startTime, endTime);
    }

    @Override
    public void createUsageEvent(LoadBalancerUsageEvent loadBalancerUsageEvent) {
        if (loadBalancerUsageEvent.getEventType().equals(EventType.CREATE_LOADBALANCER.name()) || loadBalancerUsageEvent.getEventType().equals(EventType.DELETE_LOADBALANCER.name())) {
            // Check event_table for certain duplicate events (fail-safe)
            List<LoadBalancerUsageEvent> usageEvents = loadBalancerUsageEventRepository.getFilteredEventsForLoadBalancer(loadBalancerUsageEvent.getLoadbalancerId(), UsageEvent.valueOf(loadBalancerUsageEvent.getEventType()));
            if (!usageEvents.isEmpty()) return;

            // Check hourly lb_usage table
            List<LoadBalancerUsage> loadBalancerUsages = loadBalancerUsageRepository.getRecordsForLoadBalancer(loadBalancerUsageEvent.getLoadbalancerId(), UsageEvent.valueOf(loadBalancerUsageEvent.getEventType()));
            if (!loadBalancerUsages.isEmpty()) return;

            // Check main lb_usage table
            List<Usage> usages = usageRepository.getRecordForLoadBalancer(loadBalancerUsageEvent.getLoadbalancerId(), UsageEvent.valueOf(loadBalancerUsageEvent.getEventType()));
            if (!usages.isEmpty()) return;
        }

        loadBalancerUsageEventRepository.create(loadBalancerUsageEvent);
    }
}
