package org.openstack.atlas.usagerefactor.processor.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.AccountUsage;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.VirtualIpType;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.repository.AccountUsageRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.processor.UsageEventProcessor;
import org.openstack.atlas.usagerefactor.processor.mapper.UsageEventMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.openstack.atlas.service.domain.events.UsageEvent.*;


public class UsageEventProcessorImpl implements UsageEventProcessor {
    private final Log LOG = LogFactory.getLog(UsageEventProcessorImpl.class);
    protected UsageRefactorService usageRefactorService;
    protected VirtualIpRepository virtualIpRepository;
    protected LoadBalancerRepository loadBalancerRepository;
    protected AccountUsageRepository accountUsageRepository;

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
