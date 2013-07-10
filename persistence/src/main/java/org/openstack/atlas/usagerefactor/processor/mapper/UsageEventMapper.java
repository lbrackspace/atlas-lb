package org.openstack.atlas.usagerefactor.processor.mapper;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.usagerefactor.SnmpUsage;

import java.util.Calendar;

public class UsageEventMapper {
    private LoadBalancer loadBalancer;
    private boolean isServiceNetLb;
    private SnmpUsage snmpUsage;
    private UsageEvent usageEvent;
    private Calendar pollTime;
    private Integer tagsBitmask;
    private Integer numVips;

    public UsageEventMapper(LoadBalancer loadBalancer,
                            boolean isServiceNetLb, SnmpUsage snmpUsage, UsageEvent usageEvent, Calendar polltime) {
        this.loadBalancer = loadBalancer;
        this.isServiceNetLb = isServiceNetLb;
        this.snmpUsage = snmpUsage;
        this.usageEvent = usageEvent;
        this.pollTime = polltime;
    }

    public UsageEventMapper(LoadBalancer loadBalancer, SnmpUsage usage, UsageEvent usageEvent, Calendar pollTime,
                            Integer tagsBitmask, Integer numVips) {
        this.loadBalancer = loadBalancer;
        this.snmpUsage = usage;
        this.usageEvent = usageEvent;
        this.pollTime = pollTime;
        this.tagsBitmask = tagsBitmask;
        this.numVips = numVips;
    }

    public LoadBalancerHostUsage mapSnmpUsageToUsageEvent() {
        LoadBalancerHostUsage newUsageEvent = new LoadBalancerHostUsage();

        newUsageEvent.setAccountId(loadBalancer.getAccountId());
        newUsageEvent.setLoadbalancerId(loadBalancer.getId());
        newUsageEvent.setHostId(snmpUsage.getHostId());
        newUsageEvent.setPollTime(pollTime);
        newUsageEvent.setEventType(usageEvent);
        newUsageEvent.setOutgoingTransfer(snmpUsage.getBytesOut());
        newUsageEvent.setOutgoingTransferSsl(snmpUsage.getBytesOutSsl());
        newUsageEvent.setIncomingTransfer(snmpUsage.getBytesIn());
        newUsageEvent.setIncomingTransferSsl(snmpUsage.getBytesInSsl());
        newUsageEvent.setConcurrentConnections(snmpUsage.getConcurrentConnections());
        newUsageEvent.setConcurrentConnectionsSsl(snmpUsage.getConcurrentConnectionsSsl());
        if (numVips == null) {
            numVips = loadBalancer.getLoadBalancerJoinVipSet().size();
        }
        newUsageEvent.setNumVips(numVips);

        if (tagsBitmask == null) {
            tagsBitmask = calculateTags();
        }
        newUsageEvent.setTagsBitmask(tagsBitmask);
        return newUsageEvent;
    }

    public LoadBalancerHostUsage mapPreviousUsageEvent(LoadBalancerHostUsage previousUsage) {
        LoadBalancerHostUsage newUsageEvent = new LoadBalancerHostUsage();

        newUsageEvent.setAccountId(previousUsage.getAccountId());
        newUsageEvent.setLoadbalancerId(previousUsage.getLoadbalancerId());
        newUsageEvent.setHostId(previousUsage.getHostId());
        newUsageEvent.setPollTime(pollTime);
        newUsageEvent.setEventType(usageEvent);
        newUsageEvent.setOutgoingTransfer(previousUsage.getOutgoingTransfer());
        newUsageEvent.setOutgoingTransferSsl(previousUsage.getOutgoingTransferSsl());
        newUsageEvent.setIncomingTransfer(previousUsage.getIncomingTransfer());
        newUsageEvent.setIncomingTransferSsl(previousUsage.getIncomingTransferSsl());
        newUsageEvent.setConcurrentConnections(previousUsage.getConcurrentConnections());
        newUsageEvent.setConcurrentConnectionsSsl(previousUsage.getConcurrentConnectionsSsl());
        newUsageEvent.setNumVips(loadBalancer.getLoadBalancerJoinVipSet().size());

        int tags = calculateTags();
        newUsageEvent.setTagsBitmask(tags);
        return newUsageEvent;
    }

    public int calculateTags() {
        BitTags tags = new BitTags(0);

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

        if (isServiceNetLb) {
            tags.flipTagOn(BitTag.SERVICENET_LB);
        }

        return tags.toInt();
    }
}
