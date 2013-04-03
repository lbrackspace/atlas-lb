package org.openstack.atlas.usagerefactor.helpers;


import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.usagerefactor.PolledUsageRecord;

public class RollupUsageHelper {

    public static void calculateAndSetBandwidth(Usage usageToProcess, PolledUsageRecord polledUsageRecord){
        usageToProcess.setIncomingTransferSsl(usageToProcess.getIncomingTransferSsl() + polledUsageRecord.getIncomingTransferSsl());
        usageToProcess.setOutgoingTransferSsl(usageToProcess.getOutgoingTransferSsl() + polledUsageRecord.getOutgoingTransferSsl());
        usageToProcess.setIncomingTransfer(usageToProcess.getIncomingTransfer() + polledUsageRecord.getIncomingTransfer());
        usageToProcess.setOutgoingTransfer(usageToProcess.getOutgoingTransfer() + polledUsageRecord.getOutgoingTransfer());
    }

    public static void calculateAndSetAverageConcurrentConnections(Usage usageToProcess,
                                                                   PolledUsageRecord polledUsageRecord){
        double total = usageToProcess.getAverageConcurrentConnections() * usageToProcess.getNumberOfPolls();
        double total_ssl = usageToProcess.getAverageConcurrentConnectionsSsl() * usageToProcess.getNumberOfPolls();
        total += polledUsageRecord.getConcurrentConnections();
        total_ssl += polledUsageRecord.getConcurrentConnectionsSsl();
        usageToProcess.setNumberOfPolls(usageToProcess.getNumberOfPolls() + 1);
        double newAcc = total / usageToProcess.getNumberOfPolls();
        double newAccSsl = total_ssl / usageToProcess.getNumberOfPolls();
        usageToProcess.setAverageConcurrentConnections(newAcc);
        usageToProcess.setAverageConcurrentConnectionsSsl(newAccSsl);
    }
}
