package org.openstack.atlas.usagerefactor.helpers;


import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;

public class RollupUsageHelper {

    /**
     * Adds the bandwidth counters from each LoadBalancerMergedHostUsage to the Usage record.
     * @param usageToProcess
     * @param LoadBalancerMergedHostUsage
     */
    public static void calculateAndSetBandwidth(Usage usageToProcess, LoadBalancerMergedHostUsage LoadBalancerMergedHostUsage) {
        usageToProcess.setIncomingTransferSsl(usageToProcess.getIncomingTransferSsl() + LoadBalancerMergedHostUsage.getIncomingTransferSsl());
        usageToProcess.setOutgoingTransferSsl(usageToProcess.getOutgoingTransferSsl() + LoadBalancerMergedHostUsage.getOutgoingTransferSsl());
        usageToProcess.setIncomingTransfer(usageToProcess.getIncomingTransfer() + LoadBalancerMergedHostUsage.getIncomingTransfer());
        usageToProcess.setOutgoingTransfer(usageToProcess.getOutgoingTransfer() + LoadBalancerMergedHostUsage.getOutgoingTransfer());
    }

    /**
     * Calculates the averageConcurrentConnections and sets to the Usage record.
     * averageConcurrentConnections = total connections from all LoadBalancerMergedHostUsages considered in the Usage record / number Of LoadBalancerMergedHostUsages considered;
     * @param usageToProcess
     * @param LoadBalancerMergedHostUsage
     */
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
