package org.openstack.atlas.usagerefactor.processor.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.AccountUsage;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.entities.VirtualIpType;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.DeletedStatusException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.AccountUsageRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.processor.UsageEventProcessor;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

import static org.openstack.atlas.service.domain.events.UsageEvent.*;

@Component
public class UsageEventProcessorImpl implements UsageEventProcessor {
    private final Log LOG = LogFactory.getLog(UsageEventProcessorImpl.class);
    protected UsageRefactorService usageService;
    protected UsageRefactorService usageRefactorService;
    protected VirtualIpRepository virtualIpRepository;
    protected LoadBalancerRepository loadBalancerRepository;
    protected AccountUsageRepository accountUsageRepository;

    //@Required
    public void setUsageService(UsageRefactorService usageService) {
        this.usageService = usageService;
    }

    //@Required
    public void setLoadBalancerRepository(LoadBalancerRepository loadBalancerRepository) {
        this.loadBalancerRepository = loadBalancerRepository;
    }

    //@Required
    public void setAccountUsageRepository(AccountUsageRepository accountUsageRepository) {
        this.accountUsageRepository = accountUsageRepository;
    }

    //@Required
    public void setVirtualIpRepository(VirtualIpRepository virtualIpRepository) {
        this.virtualIpRepository = virtualIpRepository;
    }

    //@Required
    public void setUsageRefactorService(UsageRefactorService usageRefactorService) {
        this.usageRefactorService = usageRefactorService;
    }

    @Override
    public void processUsageEvent(List<SnmpUsage> usages, LoadBalancer loadBalancer, UsageEvent usageEvent) {
        LOG.info(String.format("Processing '%s' usage event for load balancer '%d'...", usageEvent.name(), loadBalancer.getId()));
        Calendar pollTime = Calendar.getInstance();

        //Batch this??
        for (SnmpUsage usage : usages) {
            usageService.createUsageEvent(mapSnmpUsage(usage, loadBalancer, pollTime, usageEvent));

        }
        // If account specific event then go ahead and create entry in account usage table
        if (usageEvent.equals(CREATE_LOADBALANCER) || usageEvent.equals(DELETE_LOADBALANCER) || usageEvent.equals(CREATE_VIRTUAL_IP) || usageEvent.equals(UsageEvent.DELETE_VIRTUAL_IP)) {
            createAccountUsageEntry(loadBalancer, pollTime);
        }

        LOG.debug(String.format("Finished processing '%s' usage event for load balancer '%d'...", usageEvent.name(), loadBalancer.getId()));
    }

    @Override
    public LoadBalancerHostUsage mapSnmpUsage(SnmpUsage usage, LoadBalancer loadBalancer, Calendar pollTime, UsageEvent usageEvent) {
        LoadBalancerHostUsage newUsageEvent = new LoadBalancerHostUsage();
        newUsageEvent.setAccountId(loadBalancer.getAccountId());
        newUsageEvent.setLoadbalancerId(loadBalancer.getId());
        newUsageEvent.setHostId(usage.getHostId());
        newUsageEvent.setPollTime(pollTime);
        newUsageEvent.setEventType(usageEvent);
        newUsageEvent.setOutgoingTransfer(usage.getBytesOut());
        newUsageEvent.setOutgoingTransferSsl(usage.getBytesOutSsl());
        newUsageEvent.setIncomingTransfer(usage.getBytesIn());
        newUsageEvent.setIncomingTransferSsl(usage.getBytesInSsl());
        newUsageEvent.setConcurrentConnections(usage.getConcurrentConnections());
        newUsageEvent.setConcurrentConnectionsSsl(usage.getConcurrentConnectionsSsl());
        newUsageEvent.setNumVips(loadBalancer.getLoadBalancerJoinVipSet().size());


        int tags = calculateTags(loadBalancer.getAccountId(), loadBalancer.getId(), usageEvent, usageService.getRecentHostUsageRecord(loadBalancer.getId()));
        newUsageEvent.setTagsBitmask(tags);

        return newUsageEvent;
    }

    public void createAccountUsageEntry(LoadBalancer loadBalancer, Calendar eventTime) {
        Integer accountId = loadBalancer.getAccountId();
        AccountUsage usage = new AccountUsage();
        usage.setAccountId(accountId);
        usage.setStartTime(eventTime);
        usage.setNumLoadBalancers(loadBalancerRepository.getNumNonDeletedLoadBalancersForAccount(accountId));
        usage.setNumPublicVips(virtualIpRepository.getNumUniqueVipsForAccount(accountId, VirtualIpType.PUBLIC));
        usage.setNumServicenetVips(virtualIpRepository.getNumUniqueVipsForAccount(accountId, VirtualIpType.SERVICENET));
        accountUsageRepository.save(usage);
    }

    public int calculateTags(Integer accountId, Integer lbId, UsageEvent usageEvent, LoadBalancerHostUsage recentUsage) {
        BitTags tags;

        if (recentUsage != null) {
            tags = new BitTags(recentUsage.getTagsBitmask());
        } else {
            tags = new BitTags();
        }

        return calculateTags(accountId, lbId, usageEvent, tags);
    }

    public int calculateTags(Integer accountId, Integer lbId, UsageEvent usageEvent, BitTags bitTags) {
        BitTags tags = new BitTags(bitTags.getBitTags());

        switch (usageEvent) {
            case CREATE_LOADBALANCER:
                tags.flipAllTagsOff();
                break;
            case DELETE_LOADBALANCER:
                tags.flipTagOff(BitTag.SSL);
                tags.flipTagOff(BitTag.SSL_MIXED_MODE);
                break;
            case SSL_OFF:
                tags.flipTagOff(BitTag.SSL);
                tags.flipTagOff(BitTag.SSL_MIXED_MODE);
                break;
            case SSL_ONLY_ON:
                tags.flipTagOn(BitTag.SSL);
                tags.flipTagOff(BitTag.SSL_MIXED_MODE);
                break;
            case SSL_MIXED_ON:
                tags.flipTagOn(BitTag.SSL);
                tags.flipTagOn(BitTag.SSL_MIXED_MODE);
                break;
            default:
        }

        if (isServiceNetLoadBalancer(accountId, lbId)) {
            tags.flipTagOn(BitTag.SERVICENET_LB);
        }

        return tags.getBitTags();
    }

    public boolean isServiceNetLoadBalancer(Integer accountId, Integer lbId) {
        try {
            final Set<VirtualIp> vipsByAccountIdLoadBalancerId = loadBalancerRepository.getVipsByAccountIdLoadBalancerId(accountId, lbId);

            for (VirtualIp virtualIp : vipsByAccountIdLoadBalancerId) {
                if (virtualIp.getVipType().equals(VirtualIpType.SERVICENET)) return true;
            }

        } catch (EntityNotFoundException e) {
            return false;
        } catch (DeletedStatusException e) {
            return false;
        }

        return false;
    }
}
