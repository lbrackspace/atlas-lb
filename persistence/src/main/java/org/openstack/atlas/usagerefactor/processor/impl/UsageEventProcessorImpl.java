package org.openstack.atlas.usagerefactor.processor.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.AccountUsage;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.entities.VirtualIpType;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.AccountUsageRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.processor.UsageEventProcessor;
import org.openstack.atlas.usagerefactor.processor.mapper.UsageEventMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.openstack.atlas.service.domain.events.UsageEvent.*;


@Component
public class UsageEventProcessorImpl implements UsageEventProcessor {
    private final Log LOG = LogFactory.getLog(UsageEventProcessorImpl.class);
    protected UsageRefactorService usageRefactorService;
    protected VirtualIpRepository virtualIpRepository;
    protected LoadBalancerRepository loadBalancerRepository;
    protected AccountUsageRepository accountUsageRepository;
    protected UsageRepository usageRepository;
    protected LoadBalancerService loadBalancerService;
    protected NotificationService notificationService;

    private class TagsBitMaskAndNumVips {
        public int tagsBitmask = 1;
        public int numVips = 1;
    }

    @Autowired
    public void setLoadBalancerRepository(LoadBalancerRepository loadBalancerRepository) {
        this.loadBalancerRepository = loadBalancerRepository;
    }

    @Autowired
    public void setAccountUsageRepository(AccountUsageRepository accountUsageRepository) {
        this.accountUsageRepository = accountUsageRepository;
    }

    @Autowired
    public void setVirtualIpRepository(VirtualIpRepository virtualIpRepository) {
        this.virtualIpRepository = virtualIpRepository;
    }

    @Autowired
    public void setUsageRefactorService(UsageRefactorService usageRefactorService) {
        this.usageRefactorService = usageRefactorService;
    }

    @Autowired
    public void setUsageRepository(UsageRepository usageRepository) {
        this.usageRepository = usageRepository;
    }

    @Autowired
    public void setLoadBalancerService(LoadBalancerService loadBalancerService) {
        this.loadBalancerService = loadBalancerService;
    }

    @Autowired
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void processUsageEvent(List<SnmpUsage> usages, LoadBalancer loadBalancer, UsageEvent usageEvent, Calendar pollTime) {
        LOG.info(String.format("Processing '%s' usage event for load balancer '%d'...", usageEvent.name(),
                loadBalancer.getId()));

        int tagsBitmask = loadBalancerService.getCurrentBitTags(loadBalancer.getId()).toInt();
        int numVips = virtualIpRepository.getNumIpv4VipsForLoadBalancer(loadBalancer).intValue();

        List<LoadBalancerHostUsage> usageRecordsToCreate = new ArrayList<LoadBalancerHostUsage>();
        //If for whatever reason there were no usage records returned by any hosts for this load balancer, insert 1 record with -1 values for bandwidth
        //and the event type.
        if (usages.isEmpty()) {
            notificationService.saveAlert(loadBalancer.getAccountId(), loadBalancer.getId(), new EntityNotFoundException(),
                    AlertType.USAGE_FAILURE.name(), String.format("Usage processing for %s event failed because no hosts returned " +
                    "usage for this load balancer.  Inserting -1 for bandwidth and event anyway.", usageEvent.name()));
            SnmpUsage phantomUsage = new SnmpUsage();
            phantomUsage.setConcurrentConnections(0);
            phantomUsage.setConcurrentConnectionsSsl(0);
            phantomUsage.setBytesIn(-1L);
            phantomUsage.setBytesOut(-1L);
            phantomUsage.setBytesInSsl(-1L);
            phantomUsage.setBytesOutSsl(-1L);
            phantomUsage.setHostId(loadBalancer.getHost().getId());
            phantomUsage.setLoadbalancerId(loadBalancer.getId());
            LoadBalancerHostUsage usageRecordToProcess = new UsageEventMapper(loadBalancer, phantomUsage, usageEvent, pollTime, tagsBitmask, 0).mapSnmpUsageToUsageEvent();
            usageRefactorService.createUsageEvent(usageRecordToProcess);
        }
        for (SnmpUsage usage : usages) {
            LoadBalancerHostUsage usageRecordToProcess;

            //This usage record failed to collect from SNMP, handle accordingly...
            //If this is unsuspend event then we can assume counters start at 0
            if (isInvalidUsage(usage, usageEvent)){
                LOG.info(String.format("Snmp usage failed to collect for load balancer %d on host %d. Normal bw_out %d, Normal bw_in %d, " +
                        "Normal ccs %d, SSL bw_out %d, SSL bw_in %d, SSL ccs %d. This record will not be inserted.", usage.getLoadbalancerId(), usage.getHostId(),
                        usage.getBytesOut(), usage.getBytesIn(), usage.getConcurrentConnections(), usage.getBytesOutSsl(),
                        usage.getBytesInSsl(), usage.getConcurrentConnectionsSsl()));
                continue;
            }

            //setNegativeUsageToZero(usage);

            LOG.info(String.format("Creating usage event for load balancer '%d'...", loadBalancer.getId()));
            usageRecordToProcess = new UsageEventMapper(loadBalancer, usage, usageEvent, pollTime, tagsBitmask, numVips)
                        .mapSnmpUsageToUsageEvent();

            if(usageEvent == UsageEvent.DELETE_LOADBALANCER) {
                usageRecordToProcess.setNumVips(0);
            }

            usageRecordsToCreate.add(usageRecordToProcess);
            LOG.info(String.format("Added usage record for load balancer id '%d' to list to be inserted.",
                    loadBalancer.getId()));
        }

        if(!usageRecordsToCreate.isEmpty()){
            usageRefactorService.batchCreateLoadBalancerHostUsages(usageRecordsToCreate);
        } else {
            LOG.warn(String.format("There were no usage records created for load balancer %d for event %s. " +
                    "This is probably a problem.", loadBalancer.getId(), usageEvent.toString()));
        }

        LOG.info(String.format("Successfully inserted '%d' usage records into lb_host_usage table.", usageRecordsToCreate.size()));

        // If account specific event then go ahead and create entry in account usage table
        if (usageEvent.equals(CREATE_LOADBALANCER) || usageEvent.equals(DELETE_LOADBALANCER)
                || usageEvent.equals(CREATE_VIRTUAL_IP) || usageEvent.equals(DELETE_VIRTUAL_IP)) {
            LOG.info(String.format("Creating account usage event for load balancer '%d'...",
                    loadBalancer.getId()));
            createAccountUsageEntry(loadBalancer, pollTime);
            LOG.info(String.format("Successfully created account usage event for load balancer '%d'...",
                    loadBalancer.getId()));
        }

        LOG.debug(String.format("Finished processing '%s' usage event for load balancer '%d'...",
                usageEvent.name(), loadBalancer.getId()));
    }

    @Override
    public AccountUsage createAccountUsageEntry(LoadBalancer loadBalancer, Calendar eventTime) {
        Integer accountId = loadBalancer.getAccountId();
        AccountUsage usage = new AccountUsage();
        usage.setAccountId(accountId);
        usage.setStartTime(eventTime);
        usage.setNumLoadBalancers(loadBalancerRepository.getNumNonDeletedLoadBalancersForAccount(accountId));
        usage.setNumPublicVips(virtualIpRepository.getNumUniqueVipsForAccount(accountId, VirtualIpType.PUBLIC));
        usage.setNumServicenetVips(virtualIpRepository.getNumUniqueVipsForAccount(accountId, VirtualIpType.SERVICENET));
        accountUsageRepository.save(usage);
        return usage;
    }

    private boolean isInvalidUsage(SnmpUsage usage, UsageEvent usageEvent) {
        return usage.getBytesOut() == -1 && usage.getBytesOutSsl() == -1 && usage.getBytesIn() == -1 &&
                usage.getBytesInSsl() == -1 && usageEvent != UsageEvent.DELETE_LOADBALANCER;
    }

    private void setNegativeUsageToZero(SnmpUsage usage) {
        if(usage.getBytesOut() < 0L){
            usage.setBytesOut(0L);
        }
        if(usage.getBytesOutSsl() < 0L){
            usage.setBytesOutSsl(0L);
        }
        if(usage.getBytesIn() < 0L){
            usage.setBytesIn(0L);
        }
        if(usage.getBytesInSsl() < 0L){
            usage.setBytesInSsl(0L);
        }
        if(usage.getConcurrentConnections() < 0){
            usage.setConcurrentConnections(0);
        }
        if(usage.getConcurrentConnectionsSsl() < 0){
            usage.setConcurrentConnectionsSsl(0);
        }
    }
}
