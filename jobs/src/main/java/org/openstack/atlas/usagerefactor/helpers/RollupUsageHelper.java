package org.openstack.atlas.usagerefactor.helpers;


import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;

public class RollupUsageHelper {

    public static void calculateAndSetBandwidth(Usage usageToProcess, LoadBalancerMergedHostUsage LoadBalancerMergedHostUsage) {
        usageToProcess.setIncomingTransferSsl(usageToProcess.getIncomingTransferSsl() + LoadBalancerMergedHostUsage.getIncomingTransferSsl());
        usageToProcess.setOutgoingTransferSsl(usageToProcess.getOutgoingTransferSsl() + LoadBalancerMergedHostUsage.getOutgoingTransferSsl());
        usageToProcess.setIncomingTransfer(usageToProcess.getIncomingTransfer() + LoadBalancerMergedHostUsage.getIncomingTransfer());
        usageToProcess.setOutgoingTransfer(usageToProcess.getOutgoingTransfer() + LoadBalancerMergedHostUsage.getOutgoingTransfer());
    }

    public static void calculateAndSetAverageConcurrentConnections(Usage usageToProcess,
                                                                   LoadBalancerMergedHostUsage LoadBalancerMergedHostUsage) {
        double total = usageToProcess.getAverageConcurrentConnections() * usageToProcess.getNumberOfPolls();
        double total_ssl = usageToProcess.getAverageConcurrentConnectionsSsl() * usageToProcess.getNumberOfPolls();
        total += LoadBalancerMergedHostUsage.getConcurrentConnections();
        total_ssl += LoadBalancerMergedHostUsage.getConcurrentConnectionsSsl();
        usageToProcess.setNumberOfPolls(usageToProcess.getNumberOfPolls() + 1);
        double newAcc = total / usageToProcess.getNumberOfPolls();
        double newAccSsl = total_ssl / usageToProcess.getNumberOfPolls();
        usageToProcess.setAverageConcurrentConnections(newAcc);
        usageToProcess.setAverageConcurrentConnectionsSsl(newAccSsl);
    }
}
