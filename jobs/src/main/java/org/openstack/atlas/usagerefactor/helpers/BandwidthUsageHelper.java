package org.openstack.atlas.usagerefactor.helpers;


import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.usagerefactor.PolledUsageRecord;

public class BandwidthUsageHelper {

    public static void calculateAndSetBandwidth(Usage usageToProcess, PolledUsageRecord polledUsageRecord){
        usageToProcess.setIncomingTransferSsl(usageToProcess.getIncomingTransferSsl() + polledUsageRecord.getIncomingTransferSsl());
        usageToProcess.setOutgoingTransferSsl(usageToProcess.getOutgoingTransferSsl() + polledUsageRecord.getOutgoingTransferSsl());
        usageToProcess.setIncomingTransfer(usageToProcess.getIncomingTransfer() + polledUsageRecord.getIncomingTransfer());
        usageToProcess.setOutgoingTransfer(usageToProcess.getOutgoingTransfer() + polledUsageRecord.getOutgoingTransfer());
    }
}
