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
import org.openstack.atlas.service.domain.services.UsageRefactorService;
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

    @Override
    public void processUsageEvent(List<SnmpUsage> usages, LoadBalancer loadBalancer, UsageEvent usageEvent, Calendar pollTime) {
        LOG.info(String.format("Processing '%s' usage event for load balancer '%d'...", usageEvent.name(),
                loadBalancer.getId()));
        if(pollTime == null) {
            pollTime = Calendar.getInstance();
        }

        List<LoadBalancerHostUsage> usageRecordsToCreate = new ArrayList<LoadBalancerHostUsage>();
        for (SnmpUsage usage : usages) {
            LoadBalancerHostUsage usageRecordToProcess;
            LoadBalancerHostUsage prevUsageRecord = null;
            boolean isServicenetLb = loadBalancerRepository.isServicenetLoadBalancer(loadBalancer.getId());

            //This usage record failed to collect from SNMP, handle accordingly...
            //If this is unsuspend event then we can assume counters start at 0
            if (usage.getBytesOut() == -1 && usage.getBytesOutSsl() == -1 && usageEvent != UsageEvent.UNSUSPEND_LOADBALANCER && usageEvent != UsageEvent.DELETE_LOADBALANCER){
                LOG.info(String.format("Snmp usage failed to collect for load balancer %d on host %d. Normal bw_out %d, Normal bw_in %d, " +
                        "Normal ccs %d, SSL bw_out %d, SSL bw_in %d, SSL ccs %d. This record will not be inserted.", usage.getLoadbalancerId(), usage.getHostId(),
                        usage.getBytesOut(), usage.getBytesIn(), usage.getConcurrentConnections(), usage.getBytesOutSsl(),
                        usage.getBytesInSsl(), usage.getConcurrentConnectionsSsl()));
                continue;
            }
            if(usage.getBytesOut() == -1){
                usage.setBytesOut(0L);
            }
            if(usage.getBytesOutSsl() == -1){
                usage.setBytesOutSsl(0L);
            }
            if(usage.getBytesIn() == -1){
                usage.setBytesIn(0L);
            }
            if(usage.getBytesInSsl() == -1){
                usage.setBytesInSsl(0L);
            }
            if(usage.getConcurrentConnections() == -1){
                usage.setConcurrentConnections(0);
            }
            if(usage.getConcurrentConnectionsSsl() == -1){
                usage.setConcurrentConnectionsSsl(0);
            }
            if (usage.getHostId() != 0 && usage.getLoadbalancerId() == 0) {
                LoadBalancerHostUsage recentRecord;
                recentRecord = usageRefactorService.getLastRecordForLbIdAndHostId(loadBalancer.getId(), usage.getHostId());
                if (recentRecord != null) {
                    //Prep for new record...
                    prevUsageRecord = new UsageEventMapper(loadBalancer, isServicenetLb, null, usageEvent, pollTime)
                            .mapPreviousUsageEvent(recentRecord);
                }
            }

            LOG.info(String.format("Creating usage event for load balancer '%d'...", loadBalancer.getId()));
            if (prevUsageRecord != null) {
                usageRecordToProcess = prevUsageRecord;
            } else {
                usageRecordToProcess = new UsageEventMapper(loadBalancer,isServicenetLb, usage, usageEvent, pollTime)
                        .mapSnmpUsageToUsageEvent();
            }
            if(usageEvent == UsageEvent.DELETE_LOADBALANCER) {
                usageRecordToProcess.setNumVips(0);
            }

            int mostRecentTags;
            int mostRecentNumVips;
            LOG.info("Retrieving most recent record from lb_host_usage table for load balancer " + loadBalancer.getId());
            LoadBalancerHostUsage recentRecord = usageRefactorService.getLastRecordForLbIdAndHostId(loadBalancer.getId(), usage.getHostId());
            if (recentRecord != null) {
                mostRecentTags = recentRecord.getTagsBitmask();
                mostRecentNumVips = recentRecord.getNumVips();
            } else {
                LOG.info("lb_host_usage did not have a record for load balancer " + loadBalancer.getId() +
                        ". Attempting to pull from lb_merged_host_usage...");
                try {
                    LoadBalancerMergedHostUsage recentMergedRecord = usageRefactorService.getLastRecordForLbId(loadBalancer.getId());
                    mostRecentTags = recentMergedRecord.getTagsBitmask();
                    mostRecentNumVips = recentMergedRecord.getNumVips();
                } catch (EntityNotFoundException e) {
                    try {
                        LOG.info("lb_merged_host_usage did not have a record for load balancer " + loadBalancer.getId() +
                                 ". Attempting to pull from loadbalancing.lb_usage...");
                        Usage recentUsageRecord = usageRepository.getMostRecentUsageForLoadBalancer(loadBalancer.getId());
                        mostRecentTags = recentUsageRecord.getTags();
                        mostRecentNumVips = recentUsageRecord.getNumVips();
                    } catch (EntityNotFoundException e1) {
                        LOG.info("loadbalancing.lb_usage did not have a record for load balancer " + loadBalancer.getId() +
                                 ". Attempting to pull straight from load balancer tables.");
                        BitTags tags = loadBalancerService.getCurrentBitTags(loadBalancer.getId());
                        //We want to default to nonssl to ensure no overcharges.
                        //Servicenet tags will remain though.
                        tags.flipTagOff(BitTag.SSL);
                        tags.flipTagOff(BitTag.SSL_MIXED_MODE);
                        mostRecentTags = tags.toInt();
                        mostRecentNumVips = virtualIpRepository.getNumIpv4VipsForLoadBalancer(loadBalancer).intValue();
                    }
                }
            }


            //Handles setting of correct tags if it is not an SSL event and DELETE_LB event and CREATE_LB
            if (usageEvent != UsageEvent.SSL_OFF  && usageEvent != UsageEvent.SSL_ONLY_ON &&
                    usageEvent != UsageEvent.SSL_MIXED_ON && usageEvent != UsageEvent.DELETE_LOADBALANCER &&
                    usageEvent != UsageEvent.CREATE_LOADBALANCER) {
                    usageRecordToProcess.setTagsBitmask(mostRecentTags);
            }
            //Handles setting of correct numVips if it is not an SSL event and DELETE_LB event and CREATE_LB
            if (usageEvent != UsageEvent.CREATE_VIRTUAL_IP && usageEvent != UsageEvent.DELETE_VIRTUAL_IP &&
                    usageEvent != UsageEvent.DELETE_LOADBALANCER && usageEvent != UsageEvent.CREATE_LOADBALANCER) {
                    usageRecordToProcess.setNumVips(mostRecentNumVips);
            }

            usageRecordsToCreate.add(usageRecordToProcess);
            LOG.info(String.format("Added usage record for load balancer id '%d' to list to be inserted.",
                    loadBalancer.getId()));
        }

        if(!usageRecordsToCreate.isEmpty()){
            usageRefactorService.batchCreateLoadBalancerHostUsages(usageRecordsToCreate);
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
}
