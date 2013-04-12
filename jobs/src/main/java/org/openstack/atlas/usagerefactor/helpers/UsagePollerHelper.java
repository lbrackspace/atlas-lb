package org.openstack.atlas.usagerefactor.helpers;

import org.apache.camel.processor.loadbalancer.LoadBalancer;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.usagerefactor.SnmpUsage;

import java.util.ArrayList;
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

    public static void calculateUsage(LoadBalancerHostUsage currentRecord, LoadBalancerHostUsage previousRecord,
                                      LoadBalancerMergedHostUsage newMergedUsage) {
        long totIncomingTransfer = newMergedUsage.getIncomingTransfer();
        long totIncomingTransferSsl = newMergedUsage.getIncomingTransferSsl();
        long totOutgoingTransfer = newMergedUsage.getOutgoingTransfer();
        long totOutgoingTransferSsl = newMergedUsage.getOutgoingTransferSsl();
        double totConcurrentConnections = newMergedUsage.getConcurrentConnections();
        double totConcurrentConnectionsSsl = newMergedUsage.getConcurrentConnectionsSsl();
        if (!isReset(currentRecord, previousRecord)) {
            totIncomingTransfer += currentRecord.getIncomingTransfer() - previousRecord.getIncomingTransfer();
            totIncomingTransferSsl += currentRecord.getIncomingTransferSsl() - previousRecord.getIncomingTransferSsl();
            totOutgoingTransfer += currentRecord.getOutgoingTransfer() - previousRecord.getOutgoingTransfer();
            totOutgoingTransferSsl += currentRecord.getOutgoingTransferSsl() - previousRecord.getOutgoingTransferSsl();
            totConcurrentConnections += currentRecord.getConcurrentConnections() - previousRecord.getConcurrentConnections();
            totConcurrentConnectionsSsl += currentRecord.getConcurrentConnectionsSsl() - previousRecord.getConcurrentConnectionsSsl();
        }
        newMergedUsage.setIncomingTransfer(totIncomingTransfer);
        newMergedUsage.setIncomingTransferSsl(totIncomingTransferSsl);
        newMergedUsage.setOutgoingTransfer(totOutgoingTransfer);
        newMergedUsage.setOutgoingTransferSsl(totOutgoingTransferSsl);
        newMergedUsage.setConcurrentConnections(totConcurrentConnections);
        newMergedUsage.setConcurrentConnections(totConcurrentConnectionsSsl);
    }

    public static boolean isReset(SnmpUsage currentUsage, LoadBalancerHostUsage existingUsage) {
        return existingUsage.getIncomingTransfer() > currentUsage.getBytesIn() ||
               existingUsage.getOutgoingTransfer() > currentUsage.getBytesOut() ||
               existingUsage.getIncomingTransferSsl() > currentUsage.getBytesInSsl() ||
               existingUsage.getOutgoingTransferSsl() > currentUsage.getBytesOutSsl();
    }

    public static boolean isReset(LoadBalancerHostUsage currentRecord, LoadBalancerHostUsage previousRecord) {
        return previousRecord.getIncomingTransfer() > currentRecord.getIncomingTransfer() ||
               previousRecord.getOutgoingTransfer() > currentRecord.getOutgoingTransfer() ||
               previousRecord.getIncomingTransferSsl() > currentRecord.getIncomingTransferSsl() ||
               previousRecord.getOutgoingTransferSsl() > currentRecord.getOutgoingTransferSsl();
    }

    public static List<LoadBalancerMergedHostUsage> processExistingEvents(Map<Integer, List<LoadBalancerHostUsage>> existingUsages) {
        List<LoadBalancerMergedHostUsage> newMergedEventRecords = new ArrayList<LoadBalancerMergedHostUsage>();
        for (Integer loadBalancerId : existingUsages.keySet()) {
            if (existingUsages.get(loadBalancerId).size() > 0) {
                //If very last record in the list of loadbalancer usages is not an event, then it MUST be the records inserted
                //during the previous poll, which means no events occurred between now and previous poll.
                if (existingUsages.get(loadBalancerId).get(existingUsages.get(loadBalancerId).size() - 1).getEventType() == null) {
                    //There are no events to process so continue with next loadbalancer.
                    continue;
                }
            }
            //There must be events to process at this point
            //Create reference so the accessing isn't so wonky looking.
            List<LoadBalancerHostUsage> lbHostUsageListRef = existingUsages.get(loadBalancerId);

            //Not the best way to calculate number of hosts, but will suffice until a call to check the number of hosts is done
            int hostCount = 0;
            for (int recordIndex = 0; recordIndex < lbHostUsageListRef.size(); recordIndex++) {
                if (lbHostUsageListRef.get(recordIndex).getEventType() == null){
                    break;
                }
                hostCount++;
            }

            //increment index by the number of hosts so that the index is only skipping to each event section, and not
            ///going through all records
            for (int recordIndex = hostCount; recordIndex < lbHostUsageListRef.size(); recordIndex += hostCount) {
                //Initialize data in new record to that of current host usage record.
                LoadBalancerMergedHostUsage newLBMergedHostUsage = initializeMergedRecord(lbHostUsageListRef.get(recordIndex));

                //Iterate through the current event records and compare to previous event/polled records to calculate usage.
                for (int sameEventIndex = recordIndex; sameEventIndex < recordIndex + hostCount; sameEventIndex++) {
                    if(lbHostUsageListRef.get(sameEventIndex).getHostId() == lbHostUsageListRef.get(sameEventIndex - hostCount).getHostId()){
                        ///calculateUsage(lbHostUsageListRef.get(sameEventIndex), lbHostUsageListRef.get(sameEventIndex - hostCount), newLBMergedHostUsage);
                    }
                }

                newMergedEventRecords.add(newLBMergedHostUsage);
            }

            //Remove records that are no longer needed.
            int currentListSize = lbHostUsageListRef.size();
            for (int recordIndex = 0; recordIndex < currentListSize - hostCount; recordIndex++) {
                lbHostUsageListRef.remove(recordIndex);
            }
        }
        return newMergedEventRecords;
    }

    public static LoadBalancerMergedHostUsage initializeMergedRecord(LoadBalancerHostUsage lbHostUsage) {
        LoadBalancerMergedHostUsage newLBMergedHostUsage = new LoadBalancerMergedHostUsage();
        newLBMergedHostUsage.setAccountId(lbHostUsage.getAccountId());
        newLBMergedHostUsage.setLoadbalancerId(lbHostUsage.getLoadbalancerId());
        newLBMergedHostUsage.setNumVips(lbHostUsage.getNumVips());
        newLBMergedHostUsage.setEventType(lbHostUsage.getEventType());
        return newLBMergedHostUsage;
    }
}
