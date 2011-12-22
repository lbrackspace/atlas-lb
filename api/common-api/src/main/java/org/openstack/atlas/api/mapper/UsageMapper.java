package org.openstack.atlas.api.mapper;

import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancerUsage;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancerUsageRecord;
import org.openstack.atlas.docs.loadbalancers.api.v1.VipType;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;

import java.util.ArrayList;
import java.util.List;

public final class UsageMapper {
    public static LoadBalancerUsage toRestApiServiceUsage(List<Usage> domainUsageList) {
        LoadBalancerUsage lbusage = new LoadBalancerUsage();
        if (domainUsageList != null) {
            lbusage.getLoadBalancerUsageRecords().addAll(toRestApiUsages(domainUsageList));
        }
        return lbusage;
    }

    public static LoadBalancerUsage toRestApiCurrentUsage(List<Usage> domainUsageList) {
        LoadBalancerUsage currentUsage = new LoadBalancerUsage();
        if (domainUsageList != null) {
            currentUsage.getLoadBalancerUsageRecords().addAll(toRestApiUsages(domainUsageList));
        }
        return currentUsage;
    }

    public static List<LoadBalancerUsageRecord> toRestApiUsages(List<Usage> usageList) {
        List<LoadBalancerUsageRecord> apiUsageList = new ArrayList<LoadBalancerUsageRecord>();

        if (usageList != null) {
            for (Usage usage : usageList) {
                apiUsageList.add(toRestApiUsage(usage));
            }
        }

        return apiUsageList;
    }

    public static LoadBalancerUsageRecord toRestApiUsage(Usage dusage) {
        LoadBalancerUsageRecord rusage = new LoadBalancerUsageRecord();
        if (dusage == null) {
            return null;
        }

        rusage.setId(dusage.getId());
        rusage.setAverageNumConnections(dusage.getAverageConcurrentConnections());
        rusage.setIncomingTransfer(dusage.getIncomingTransfer());
        rusage.setOutgoingTransfer(dusage.getOutgoingTransfer());
        rusage.setNumVips(dusage.getNumVips());
        rusage.setNumPolls(dusage.getNumberOfPolls());
        rusage.setStartTime(dusage.getStartTime());
        rusage.setEndTime(dusage.getEndTime());
        rusage.setEventType(dusage.getEventType());
        // rusage.setSslTermination((dusage.getTags() & BitTags.BIT_TAG_SSL) == 1); Hidden per JIRA:SITESLB-687

        BitTags bitTags = new BitTags(dusage.getTags());

        if (bitTags.isTagOn(BitTag.SERVICENET_LB)) {
            rusage.setVipType(VipType.SERVICENET);
        } else {
            rusage.setVipType(VipType.PUBLIC);
        }

        return rusage;
    }
}
