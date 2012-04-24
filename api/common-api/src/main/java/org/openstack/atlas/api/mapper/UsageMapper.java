package org.openstack.atlas.api.mapper;

import org.openstack.atlas.docs.loadbalancers.api.v1.AccountUsage;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancerUsage;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancerUsageRecord;
import org.openstack.atlas.docs.loadbalancers.api.v1.VipType;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.entities.SslMode;
import org.openstack.atlas.service.domain.pojos.LoadBalancerBilling;
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
        rusage.setAverageNumConnectionsSsl(dusage.getAverageConcurrentConnectionsSsl());
        rusage.setIncomingTransferSsl(dusage.getIncomingTransferSsl());
        rusage.setOutgoingTransferSsl(dusage.getOutgoingTransferSsl());
        rusage.setNumVips(dusage.getNumVips());
        rusage.setNumPolls(dusage.getNumberOfPolls());
        rusage.setStartTime(dusage.getStartTime());
        rusage.setEndTime(dusage.getEndTime());
        rusage.setEventType(dusage.getEventType());

        BitTags bitTags = new BitTags(dusage.getTags());

        rusage.setSslMode(SslMode.getMode(bitTags).name());

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
        rusage.setAverageNumConnectionsSsl(dusage.getAverageConcurrentConnectionsSsl());
        rusage.setIncomingTransferSsl(dusage.getIncomingTransferSsl());
        rusage.setOutgoingTransferSsl(dusage.getOutgoingTransferSsl());
        rusage.setNumVips(dusage.getNumVips());
        rusage.setNumPolls(dusage.getNumberOfPolls());
        rusage.setStartTime(dusage.getStartTime());
        rusage.setEndTime(dusage.getEndTime());
        rusage.setEventType(dusage.getEventType());

        BitTags bitTags = new BitTags(dusage.getTags());

        rusage.setSslMode(SslMode.getMode(bitTags).name());

        if (bitTags.isTagOn(BitTag.SERVICENET_LB)) {
            rusage.setVipType(VipType.SERVICENET);
        } else {
            rusage.setVipType(VipType.PUBLIC);
        }

        return rusage;
    }

    public static org.openstack.atlas.docs.loadbalancers.api.v1.AccountBilling toDataModelAccountBilling(org.openstack.atlas.service.domain.pojos.AccountBilling domainAccountBilling) {
        org.openstack.atlas.docs.loadbalancers.api.v1.AccountBilling dataModelAccountBilling = new org.openstack.atlas.docs.loadbalancers.api.v1.AccountBilling();
        AccountUsage accountUsage = new AccountUsage();

        for (org.openstack.atlas.service.domain.entities.AccountUsage domainAccountUsage : domainAccountBilling.getAccountUsageRecords()) {
            org.openstack.atlas.docs.loadbalancers.api.v1.AccountUsageRecord accountUsageRecord = new org.openstack.atlas.docs.loadbalancers.api.v1.AccountUsageRecord();
            accountUsageRecord.setNumLoadBalancers(domainAccountUsage.getNumLoadBalancers());
            accountUsageRecord.setNumPublicVips(domainAccountUsage.getNumPublicVips());
            accountUsageRecord.setNumServicenetVips(domainAccountUsage.getNumServicenetVips());
            accountUsageRecord.setStartTime(domainAccountUsage.getStartTime());
            accountUsage.getAccountUsageRecords().add(accountUsageRecord);
        }
        dataModelAccountBilling.setAccountUsage(accountUsage);
        dataModelAccountBilling.getLoadBalancerUsages().addAll(loadBalancerBillingsToLoadBalancerUsages(domainAccountBilling.getLoadBalancerBillings()));
        dataModelAccountBilling.setAccountId(domainAccountBilling.getAccountId());

        return dataModelAccountBilling;
    }

    public static List<LoadBalancerUsage> loadBalancerBillingsToLoadBalancerUsages(List<LoadBalancerBilling> loadBalancerBillings) {
        List<LoadBalancerUsage> loadBalancerUsages = new ArrayList<LoadBalancerUsage>();

        for (LoadBalancerBilling loadBalancerBilling : loadBalancerBillings) {
            LoadBalancerUsage loadBalancerUsage = new LoadBalancerUsage();
            loadBalancerUsage.setLoadBalancerId(loadBalancerBilling.getLoadBalancerId());
            loadBalancerUsage.setLoadBalancerName(loadBalancerBilling.getLoadBalancerName());
            for (Usage usage : loadBalancerBilling.getUsageRecords()) {
                loadBalancerUsage.getLoadBalancerUsageRecords().add(toRestApiUsage(usage));
            }
            loadBalancerUsages.add(loadBalancerUsage);
        }

        return loadBalancerUsages;
    }
}
