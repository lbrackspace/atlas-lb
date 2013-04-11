package org.openstack.atlas.usagerefactor.processor.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.services.UsageService;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.processor.UsageEventProcessor;
import org.springframework.beans.factory.annotation.Required;

import java.util.Calendar;
import java.util.List;

public class UsageEventProcessorImpl implements UsageEventProcessor {
    private final Log LOG = LogFactory.getLog(UsageEventProcessorImpl.class);

    protected UsageService usageService;

    @Required
    public void setUsageService(UsageService usageService) {
        this.usageService = usageService;
    }

    @Override
    public void processUsageEvent(List<SnmpUsage> usages, LoadBalancer loadBalancer, UsageEvent usageEvent) {
        LOG.info(String.format("Processing '%s' usage event for load balancer '%d'...", usageEvent.name(), loadBalancer.getId()));
        Calendar pollTime = Calendar.getInstance();

        //Batch this??
        for (SnmpUsage usage : usages) {
            usageService.createUsageEvent(mapSnmpUsage(usage, loadBalancer, pollTime, usageEvent));
        }

        LOG.debug(String.format("Finished processing '%s' usage event for load balancer '%d'...", usageEvent.name(), loadBalancer.getId()));
    }

    @Override
    public LoadBalancerMergedHostUsage mapSnmpUsage(SnmpUsage usage, LoadBalancer loadBalancer, Calendar pollTime, UsageEvent usageEvent) {
        LoadBalancerMergedHostUsage usageRecord = new LoadBalancerMergedHostUsage();
        usageRecord.setAccountId(loadBalancer.getAccountId());
        usageRecord.setLoadbalancerId(loadBalancer.getId());
        usage.setHostId(usage.getHostId());
        usageRecord.setPollTime(pollTime);
        usageRecord.setEventType(usageEvent);
        usageRecord.setOutgoingTransfer(usage.getBytesOut());
        usageRecord.setOutgoingTransferSsl(usage.getBytesOutSsl());
        usageRecord.setIncomingTransfer(usage.getBytesIn());
        usageRecord.setIncomingTransferSsl(usage.getBytesInSsl());
        usageRecord.setConcurrentConnections(usage.getConcurrentConnections());
        usageRecord.setConcurrentConnectionsSsl(usage.getConcurrentConnectionsSsl());
//        usageRecord.setTagsBitmask();
//        usageRecord.setNumVips();
        return usageRecord;
    }
}
