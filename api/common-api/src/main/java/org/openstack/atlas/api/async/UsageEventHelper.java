package org.openstack.atlas.api.async;

import org.openstack.atlas.service.domain.entities.AccountUsage;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.VirtualIpType;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.UnauthorizedException;
import org.openstack.atlas.service.domain.repository.AccountUsageRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.service.domain.services.UsageService;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsageEvent;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageEventRepository;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.openstack.atlas.service.domain.events.UsageEvent.*;

public class UsageEventHelper {
    private final Log LOG = LogFactory.getLog(UsageEventHelper.class);
    protected UsageService usageService;
    protected LoadBalancerRepository loadBalancerRepository;
    protected AccountUsageRepository accountUsageRepository;
    protected VirtualIpRepository virtualIpRepository;

    @Required
    public void setUsageService(UsageService usageService) {
        this.usageService = usageService;
    }

    @Required
    public void setLoadBalancerRepository(LoadBalancerRepository loadBalancerRepository) {
        this.loadBalancerRepository = loadBalancerRepository;
    }

    @Required
    public void setAccountUsageRepository(AccountUsageRepository accountUsageRepository) {
        this.accountUsageRepository = accountUsageRepository;
    }

    @Required
    public void setVirtualIpRepository(VirtualIpRepository virtualIpRepository) {
        this.virtualIpRepository = virtualIpRepository;
    }

    public void processUsageEvent(LoadBalancer loadBalancer, UsageEvent usageEvent, Calendar eventTime) throws Exception {
        processUsageEvent(loadBalancer, usageEvent, null, null, null, null, null, null, eventTime);
    }

    public void processUsageEvent(LoadBalancer loadBalancer, UsageEvent usageEvent, Long bytesOut, Long bytesIn, Integer concurrentConns, Long bytesOutSsl, Long bytesInSsl, Integer concurrentConnsSsl, Calendar eventTime) {
        LOG.info(String.format("Processing '%s' usage event for load balancer '%d'...", usageEvent.name(), loadBalancer.getId()));

        LoadBalancerUsageEvent newUsageEvent = new LoadBalancerUsageEvent();
        newUsageEvent.setAccountId(loadBalancer.getAccountId());
        newUsageEvent.setLoadbalancerId(loadBalancer.getId());
        newUsageEvent.setStartTime(eventTime);
        newUsageEvent.setNumVips(loadBalancer.getLoadBalancerJoinVipSet().size());
        newUsageEvent.setEventType(usageEvent.name());
        newUsageEvent.setLastBandwidthBytesOut(bytesOut);
        newUsageEvent.setLastBandwidthBytesIn(bytesIn);
        newUsageEvent.setLastConcurrentConnections(concurrentConns);
        newUsageEvent.setLastBandwidthBytesOutSsl(bytesOutSsl);
        newUsageEvent.setLastBandwidthBytesInSsl(bytesInSsl);
        newUsageEvent.setLastConcurrentConnectionsSsl(concurrentConnsSsl);

        usageService.createUsageEvent(newUsageEvent);

        // If account specific event then create entry in account usage table
        if (usageEvent.equals(CREATE_LOADBALANCER) || usageEvent.equals(DELETE_LOADBALANCER) || usageEvent.equals(CREATE_VIRTUAL_IP) || usageEvent.equals(UsageEvent.DELETE_VIRTUAL_IP)) {
            createAccountUsageEntry(loadBalancer, eventTime);
        }

        LOG.info(String.format("'%s' usage event processed for load balancer '%d'.", usageEvent.name(), loadBalancer.getId()));
    }

    private void createAccountUsageEntry(LoadBalancer loadBalancer, Calendar eventTime) {
        Integer accountId = loadBalancer.getAccountId();
        AccountUsage usage = new AccountUsage();
        usage.setAccountId(accountId);
        usage.setStartTime(eventTime);
        usage.setNumLoadBalancers(loadBalancerRepository.getNumNonDeletedLoadBalancersForAccount(accountId));
        usage.setNumPublicVips(virtualIpRepository.getNumUniqueVipsForAccount(accountId, VirtualIpType.PUBLIC));
        usage.setNumServicenetVips(virtualIpRepository.getNumUniqueVipsForAccount(accountId, VirtualIpType.SERVICENET));
        accountUsageRepository.save(usage);
    }
}
