package org.openstack.atlas.usagerefactor.helpers;


import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.usagerefactor.PolledUsageRecord;

public class BandwidthUsageHelper {

    public static void calculateAndSetBandwidth(Usage usageToProcess, PolledUsageRecord polledUsageRecord){
        usageToProcess.setIncomingTransfer(usageToProcess.getIncomingTransfer() + polledUsageRecord.getBandwidthIn());
        usageToProcess.setOutgoingTransfer(usageToProcess.getOutgoingTransfer() + polledUsageRecord.getBandwidthOut());
        usageToProcess.setIncomingTransferSsl(usageToProcess.getIncomingTransferSsl() + polledUsageRecord.getBandwidthInSsl());
        usageToProcess.setOutgoingTransferSsl(usageToProcess.getOutgoingTransferSsl() + polledUsageRecord.getBandwidthOutSsl());
    }
// Old implementation.
//    public static void calculateAndSetBandwidth(PolledUsageRecord startPolledUsageRecord, PolledUsageRecord endPolledUsageRecord, Usage usageToProcess){
//        if (BandwidthUsageHelper.isReset(startPolledUsageRecord.getBandwidthOut(), endPolledUsageRecord.getBandwidthOut())){
//            usageToProcess.setOutgoingTransfer(startPolledUsageRecord.getBandwidthOut() + usageToProcess.getOutgoingTransfer());
//        } else {
//            usageToProcess.setOutgoingTransfer(endPolledUsageRecord.getBandwidthOut() - startPolledUsageRecord.getBandwidthOut() + usageToProcess.getOutgoingTransfer());
//        }
//        if (BandwidthUsageHelper.isReset(startPolledUsageRecord.getBandwidthOutSsl(), endPolledUsageRecord.getBandwidthOutSsl())){
//            usageToProcess.setOutgoingTransferSsl(startPolledUsageRecord.getBandwidthOutSsl() + usageToProcess.getOutgoingTransferSsl());
//        } else {
//            usageToProcess.setOutgoingTransferSsl(endPolledUsageRecord.getBandwidthOutSsl() - startPolledUsageRecord.getBandwidthOutSsl() + usageToProcess.getOutgoingTransferSsl());
//        }
//        if (BandwidthUsageHelper.isReset(startPolledUsageRecord.getBandwidthIn(), endPolledUsageRecord.getBandwidthIn())){
//            usageToProcess.setIncomingTransfer(startPolledUsageRecord.getBandwidthIn() + usageToProcess.getIncomingTransfer());
//        } else {
//            usageToProcess.setIncomingTransfer(endPolledUsageRecord.getBandwidthIn() - startPolledUsageRecord.getBandwidthIn() + usageToProcess.getIncomingTransfer());
//        }
//        if (BandwidthUsageHelper.isReset(startPolledUsageRecord.getBandwidthInSsl(), endPolledUsageRecord.getBandwidthInSsl())){
//            usageToProcess.setIncomingTransferSsl(startPolledUsageRecord.getBandwidthInSsl() + usageToProcess.getIncomingTransferSsl());
//        } else {
//            usageToProcess.setIncomingTransferSsl(endPolledUsageRecord.getBandwidthInSsl() - startPolledUsageRecord.getBandwidthInSsl() + usageToProcess.getIncomingTransferSsl());
//        }
//    }
//
//    public static boolean isReset(long startBandwidth, long endBandwidth){
//        if (endBandwidth < startBandwidth){
//            return true;
//        //TODO: check thresholds or even better, find a way to check last time zeus reset
//        } else if (endBandwidth == startBandwidth){
//            return false;
//        } else{
//            return false;
//        }
//    }

}
