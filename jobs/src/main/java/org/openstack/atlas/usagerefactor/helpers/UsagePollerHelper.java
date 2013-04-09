package org.openstack.atlas.usagerefactor.helpers;

import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.usagerefactor.SnmpUsage;

import java.util.List;
import java.util.Map;

public class UsagePollerHelper {

    public static void calculateUsage(Map<Integer, SnmpUsage> currentUsages,
                                                       Map<Integer, LoadBalancerHostUsage> existingUsages,
                                                       Map<Integer, LoadBalancerMergedHostUsage> newMergedHostUsages) {
        LoadBalancerHostUsage newLBHostUsage = new LoadBalancerHostUsage();
        for (Integer loadBalancerId : currentUsages.keySet()) {
            if (!newMergedHostUsages.containsKey(loadBalancerId)) {
                newMergedHostUsages.put(loadBalancerId, new LoadBalancerMergedHostUsage());
            }
            if (isReset(currentUsages.get(loadBalancerId), existingUsages.get(loadBalancerId))) {
                newLBHostUsage.setIncomingTransfer(0);
                newLBHostUsage.setIncomingTransferSsl(0);
                newLBHostUsage.setOutgoingTransfer(0);
                newLBHostUsage.setOutgoingTransferSsl(0);
                newLBHostUsage.setConcurrentConnections(currentUsages.get(loadBalancerId).getConcurrentConnections());
                newLBHostUsage.setConcurrentConnectionsSsl(currentUsages.get(loadBalancerId).getConcurrentConnectionsSsl());
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
