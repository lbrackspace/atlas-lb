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

    public UsageEventMapper(LoadBalancer loadBalancer,
                            boolean isServiceNetLb, SnmpUsage snmpUsage, UsageEvent usageEvent, Calendar polltime) {
        this.loadBalancer = loadBalancer;
        this.isServiceNetLb = isServiceNetLb;
        this.snmpUsage = snmpUsage;
        this.usageEvent = usageEvent;
        this.pollTime = polltime;
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

        return tags.getBitTags();
    }
}
