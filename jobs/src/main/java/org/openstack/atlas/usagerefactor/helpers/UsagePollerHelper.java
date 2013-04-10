package org.openstack.atlas.usagerefactor.helpers;

import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.usagerefactor.SnmpUsage;

import java.util.List;
import java.util.Map;

public class UsagePollerHelper {

    public static void calculateUsage(Map<Integer, SnmpUsage> currentUsages,
                                      Map<Integer, LoadBalancerHostUsage> existingUsages,
                                      Map<Integer, LoadBalancerHostUsage> currentUsageToInsert,
                                      List<LoadBalancerMergedHostUsage> newMergedHostUsages) {
        long totIncomingTransfer = 0;
        long totIncomingTransferSsl = 0;
        long totOutgoingTransfer = 0;
        long totOutgoingTransferSsl = 0;
        long totConcurrentConnections = 0;
        long totConcurrentConnectionsSsl = 0;
        for (Integer hostId : currentUsages.keySet()) {
            LoadBalancerHostUsage newLBHostUsage = new LoadBalancerHostUsage();
            newLBHostUsage.setAccountId(existingUsages.get(hostId).getAccountId());
            newLBHostUsage.setLoadbalancerId(currentUsages.get(hostId).getLoadbalancerId());
            newLBHostUsage.setTagsBitmask(existingUsages.get(hostId).getTagsBitmask());
            if (isReset(currentUsages.get(hostId), existingUsages.get(hostId))) {
                newLBHostUsage.setIncomingTransfer(0);
                newLBHostUsage.setIncomingTransferSsl(0);
                newLBHostUsage.setOutgoingTransfer(0);
                newLBHostUsage.setOutgoingTransferSsl(0);
                newLBHostUsage.setConcurrentConnections(currentUsages.get(hostId).getConcurrentConnections());
                newLBHostUsage.setConcurrentConnectionsSsl(currentUsages.get(hostId).getConcurrentConnectionsSsl());
            } else {
                totIncomingTransfer += currentUsages.get(hostId).getBytesIn() - existingUsages.get(hostId).getIncomingTransfer();
                totIncomingTransferSsl += currentUsages.get(hostId).getBytesInSsl() - existingUsages.get(hostId).getIncomingTransferSsl();
                totOutgoingTransfer += currentUsages.get(hostId).getBytesOut() - existingUsages.get(hostId).getOutgoingTransfer();
                totOutgoingTransferSsl += currentUsages.get(hostId).getBytesOutSsl() - existingUsages.get(hostId).getOutgoingTransferSsl();
                totConcurrentConnections += currentUsages.get(hostId).getConcurrentConnections() - existingUsages.get(hostId).getConcurrentConnections();
                totConcurrentConnectionsSsl += currentUsages.get(hostId).getConcurrentConnectionsSsl() - existingUsages.get(hostId).getConcurrentConnectionsSsl();
            }
        }

    }

    public static boolean isReset(SnmpUsage currentUsage, LoadBalancerHostUsage existingUsage) {
        return existingUsage.getIncomingTransfer() > currentUsage.getBytesIn() ||
               existingUsage.getOutgoingTransfer() > currentUsage.getBytesOut() ||
               existingUsage.getIncomingTransferSsl() > currentUsage.getBytesInSsl() ||
               existingUsage.getOutgoingTransferSsl() > currentUsage.getBytesOutSsl();
    }
}
