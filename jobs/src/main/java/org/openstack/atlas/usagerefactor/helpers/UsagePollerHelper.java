package org.openstack.atlas.usagerefactor.helpers;

import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.usagerefactor.SnmpUsage;

import java.util.List;
import java.util.Map;

public class UsagePollerHelper {

    public static void calculateUsage(Map<Integer, SnmpUsage> currentUsages, Map<Integer, LoadBalancerHostUsage> existingUsages) {
        for (Integer loadBalancerId : currentUsages.keySet()) {
            
        }
        //LoadBalancerMergedHostUsage mergedUsage = new LoadBalancerMergedHostUsage();
    }

    public static boolean isReset(SnmpUsage currentUsage, LoadBalancerHostUsage existingUsage) {
        if (existingUsage.getIncomingTransfer() > currentUsage.getBytesIn() ||
            existingUsage.getOutgoingTransfer() > currentUsage.getBytesOut() ||
            existingUsage.getIncomingTransferSsl() > currentUsage.getBytesInSsl() ||
            existingUsage.getOutgoingTransferSsl() > currentUsage.getBytesOutSsl()){
            return true;
        }
        return false;
    }
}
