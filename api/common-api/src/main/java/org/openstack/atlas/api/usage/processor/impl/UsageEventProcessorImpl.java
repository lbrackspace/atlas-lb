package org.openstack.atlas.api.usage.processor.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.usage.processor.UsageEventProcessor;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.services.UsageService;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.springframework.beans.factory.annotation.Required;

import java.util.Calendar;

public class UsageEventProcessorImpl implements UsageEventProcessor {
    private final Log LOG = LogFactory.getLog(UsageEventProcessorImpl.class);

    protected UsageService usageService;

    @Required
    public void setUsageService(UsageService usageService) {
        this.usageService = usageService;
    }

    @Override
    public void processUsageEvent(SnmpUsage usage, LoadBalancer loadBalancer, UsageEvent usageEvent) {
        LOG.info(String.format("Processing '%s' usage event for load balancer '%d'...", usageEvent.name(), loadBalancer.getId()));
        Calendar pollTime = Calendar.getInstance();

        LoadBalancerHostUsage usageRecord = new LoadBalancerHostUsage();
        usageRecord.setAccountId(loadBalancer.getAccountId());
        usageRecord.setLoadbalancerId(loadBalancer.getId());
        usageRecord.setUserName(loadBalancer.getUserName());
        usageRecord.setPollTime(pollTime);
        usageRecord.setEventType(usageEvent);
        usageRecord.setOutgoingTransfer(usage.getBytesOut());
        usageRecord.setOutgoingTransferSsl(usage.getBytesOutSsl());
        usageRecord.setIncomingTransfer(usage.getBytesIn());
        usageRecord.setIncomingTransferSsl(usage.getBytesInSsl());
        usageRecord.setConcurrentConnections(usage.getConcurrentConnections());
        usageRecord.setConcurrentConnectionsSsl(usage.getConcurrentConnectionsSsl());
        usageService.createUsageEvent(usageRecord);

        LOG.debug(String.format("Finished processing '%s' usage event for load balancer '%d'...", usageEvent.name(), loadBalancer.getId()));
    }
}
