package org.openstack.atlas.api.mapper;

import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancerUsage;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancerUsageRecord;
import org.openstack.atlas.docs.loadbalancers.api.v1.VipType;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        // rusage.updateSslTermination((dusage.getTags() & BitTags.BIT_TAG_SSL) == 1); Hidden per JIRA:SITESLB-687

        BitTags bitTags = new BitTags(dusage.getTags());

        if (bitTags.isTagOn(BitTag.SERVICENET_LB)) {
            rusage.setVipType(VipType.SERVICENET);
        } else {
            rusage.setVipType(VipType.PUBLIC);
        }

        return rusage;
    }

    public static List<org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerUsageRecord> toMgmtApiUsages(List<Usage> usageList) {
        List<org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerUsageRecord> apiUsageList = new ArrayList<org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerUsageRecord>();

        if (usageList != null) {
            for (Usage usage : usageList) {
                apiUsageList.add(toMgmtApiUsage(usage));
            }
        }

        return apiUsageList;
    }

    public static org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerUsageRecord toMgmtApiUsage(Usage dusage) {
        org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerUsageRecord rusage = new org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerUsageRecord();
        if (dusage == null) {
            return null;
        }

        rusage.setId(dusage.getId());
        rusage.setAccountId(dusage.getAccountId());
        rusage.setLoadBalancerId(dusage.getLoadbalancer().getId());
        rusage.setAverageNumConnections(dusage.getAverageConcurrentConnections());
        rusage.setIncomingTransfer(dusage.getIncomingTransfer());
        rusage.setOutgoingTransfer(dusage.getOutgoingTransfer());
        rusage.setNumVips(dusage.getNumVips());
        rusage.setNumPolls(dusage.getNumberOfPolls());
        rusage.setStartTime(dusage.getStartTime());
        rusage.setEndTime(dusage.getEndTime());
        rusage.setEventType(dusage.getEventType());

        BitTags bitTags = new BitTags(dusage.getTags());

        rusage.setHasSsl(bitTags.isTagOn(BitTag.SSL));
        
        if (bitTags.isTagOn(BitTag.SERVICENET_LB)) {
            rusage.setVipType(VipType.SERVICENET);
        } else {
            rusage.setVipType(VipType.PUBLIC);
        }

        return rusage;
    }
}
