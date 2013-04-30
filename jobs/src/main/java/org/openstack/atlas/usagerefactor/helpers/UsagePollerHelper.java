package org.openstack.atlas.usagerefactor.helpers;

import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.services.impl.BaseService;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.UsageProcessor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class UsagePollerHelper{

    private int numHosts;

    public UsagePollerHelper() {}

    public UsagePollerHelper(int numHosts) {
        this.numHosts = numHosts;
    }

    public int getNumHosts() {
        return numHosts;
    }

    public void setNumHosts(int numHosts) {
        this.numHosts = numHosts;
    }

    public void calculateUsage(SnmpUsage currentUsage, LoadBalancerHostUsage previousRecord,
                               LoadBalancerMergedHostUsage newMergedUsage) {
        long totIncomingTransfer = newMergedUsage.getIncomingTransfer();
        long totIncomingTransferSsl = newMergedUsage.getIncomingTransferSsl();
        long totOutgoingTransfer = newMergedUsage.getOutgoingTransfer();
        long totOutgoingTransferSsl = newMergedUsage.getOutgoingTransferSsl();
        //Handle normal virtual server resetting
        if (!isReset(currentUsage.getBytesIn(), previousRecord.getIncomingTransfer()) &&
            !isReset(currentUsage.getBytesOut(), previousRecord.getOutgoingTransfer())) {
            totIncomingTransfer += currentUsage.getBytesIn() - previousRecord.getIncomingTransfer();
            totOutgoingTransfer += currentUsage.getBytesOut() - previousRecord.getOutgoingTransfer();
        }
        //Handle SSL virtual server resetting
        if (!isReset(currentUsage.getBytesInSsl(), previousRecord.getIncomingTransferSsl()) &&
            !isReset(currentUsage.getBytesOutSsl(), previousRecord.getOutgoingTransferSsl())) {
            totIncomingTransferSsl += currentUsage.getBytesInSsl() - previousRecord.getIncomingTransferSsl();
            totOutgoingTransferSsl += currentUsage.getBytesOutSsl() - previousRecord.getOutgoingTransferSsl();
        }
        newMergedUsage.setIncomingTransfer(totIncomingTransfer);
        newMergedUsage.setIncomingTransferSsl(totIncomingTransferSsl);
        newMergedUsage.setOutgoingTransfer(totOutgoingTransfer);
        newMergedUsage.setOutgoingTransferSsl(totOutgoingTransferSsl);
        //Using concurrent connections regardless of reset since this is not a counter, only a snapshot
        int ccs = currentUsage.getConcurrentConnections() + newMergedUsage.getConcurrentConnections();
        int ccsSsl = currentUsage.getConcurrentConnectionsSsl() + newMergedUsage.getConcurrentConnectionsSsl();
        newMergedUsage.setConcurrentConnections(ccs);
        newMergedUsage.setConcurrentConnectionsSsl(ccsSsl);
    }

    public void calculateUsage(LoadBalancerHostUsage currentRecord, LoadBalancerHostUsage previousRecord,
                               LoadBalancerMergedHostUsage newMergedUsage) {
        long totIncomingTransfer = newMergedUsage.getIncomingTransfer();
        long totIncomingTransferSsl = newMergedUsage.getIncomingTransferSsl();
        long totOutgoingTransfer = newMergedUsage.getOutgoingTransfer();
        long totOutgoingTransferSsl = newMergedUsage.getOutgoingTransferSsl();
        //Handle normal virtual server resetting
        if (!isReset(currentRecord.getIncomingTransfer(), previousRecord.getIncomingTransfer()) &&
            !isReset(currentRecord.getOutgoingTransfer(), previousRecord.getOutgoingTransfer())) {
            totIncomingTransfer += currentRecord.getIncomingTransfer() - previousRecord.getIncomingTransfer();
            totOutgoingTransfer += currentRecord.getOutgoingTransfer() - previousRecord.getOutgoingTransfer();
        }
        //Handle SSL virtual server resetting
        if (!isReset(currentRecord.getIncomingTransferSsl(), previousRecord.getIncomingTransferSsl()) &&
            !isReset(currentRecord.getOutgoingTransferSsl(), previousRecord.getOutgoingTransferSsl())) {
            totIncomingTransferSsl += currentRecord.getIncomingTransferSsl() - previousRecord.getIncomingTransferSsl();
            totOutgoingTransferSsl += currentRecord.getOutgoingTransferSsl() - previousRecord.getOutgoingTransferSsl();
        }
        newMergedUsage.setIncomingTransfer(totIncomingTransfer);
        newMergedUsage.setIncomingTransferSsl(totIncomingTransferSsl);
        newMergedUsage.setOutgoingTransfer(totOutgoingTransfer);
        newMergedUsage.setOutgoingTransferSsl(totOutgoingTransferSsl);
        //Using concurrent connections regardless of reset since this is not a counter, only a snapshot
        int ccs = currentRecord.getConcurrentConnections() + newMergedUsage.getConcurrentConnections();
        int ccsSsl = currentRecord.getConcurrentConnectionsSsl() + newMergedUsage.getConcurrentConnectionsSsl();
        newMergedUsage.setConcurrentConnections(ccs);
        newMergedUsage.setConcurrentConnectionsSsl(ccsSsl);
    }

    public boolean isReset(long currentBandwidth, long previousBandwidth) {
        return currentBandwidth < previousBandwidth;
    }

    public UsageProcessorResult processCurrentUsage(Map<Integer, List<LoadBalancerHostUsage>> existingUsages,
                                                    Map<Integer, Map<Integer, SnmpUsage>> currentUsages,
                                                    Calendar pollTime){
        List<LoadBalancerMergedHostUsage> mergedUsages = new ArrayList<LoadBalancerMergedHostUsage>();
        List<LoadBalancerHostUsage> newLBHostUsages = new ArrayList<LoadBalancerHostUsage>();

        for (Integer loadbalancerId : existingUsages.keySet()) {
            if(!currentUsages.containsKey(loadbalancerId)) {
                if(existingUsages.get(loadbalancerId).get(0).getEventType() != UsageEvent.DELETE_LOADBALANCER ||
                   existingUsages.get(loadbalancerId).get(0).getEventType() != UsageEvent.SUSPEND_LOADBALANCER) {
                    LoadBalancerHostUsage existingUsage = existingUsages.get(loadbalancerId).get(0);
                    newLBHostUsages.add(convertSnmpUsageToLBHostUsage(null, existingUsage, pollTime));
                }
                continue;
            }
            LoadBalancerMergedHostUsage newMergedUsage = initializeMergedRecord(existingUsages.get(loadbalancerId).get(0));
            newMergedUsage.setEventType(null);
            newMergedUsage.setPollTime(pollTime);
            for (LoadBalancerHostUsage existingUsage : existingUsages.get(loadbalancerId)) {
                SnmpUsage currentUsage = currentUsages.get(loadbalancerId).get(existingUsage.getHostId());
                calculateUsage(currentUsage, existingUsage, newMergedUsage);
                newLBHostUsages.add(convertSnmpUsageToLBHostUsage(currentUsage, existingUsage, pollTime));
            }
            mergedUsages.add(newMergedUsage);
        }

        return new UsageProcessorResult(mergedUsages, newLBHostUsages);
    }
    public List<LoadBalancerMergedHostUsage> processExistingEvents(Map<Integer, List<LoadBalancerHostUsage>> existingUsages) {
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

            //increment index by the number of hosts so that the index is only skipping to each event section, and not
            ///going through all records
            for (int recordIndex = numHosts; recordIndex < lbHostUsageListRef.size(); recordIndex += numHosts) {
                //Initialize data in new record to that of current host usage record.
                LoadBalancerMergedHostUsage newLBMergedHostUsage = initializeMergedRecord(lbHostUsageListRef.get(recordIndex));

                ///Iterate through the current event records and compare to previous event/polled records to calculate usage.
                for (int eventIndex = recordIndex; eventIndex < recordIndex + numHosts; eventIndex++) {
                    if(lbHostUsageListRef.get(eventIndex).getHostId() == lbHostUsageListRef.get(eventIndex - numHosts).getHostId()){
                        calculateUsage(lbHostUsageListRef.get(eventIndex),
                                       lbHostUsageListRef.get(eventIndex - numHosts),
                                       newLBMergedHostUsage);
                    }
                }

                newMergedEventRecords.add(newLBMergedHostUsage);
            }

            //Remove records that are no longer needed.
            //Can definitely optimize this
            while(lbHostUsageListRef.size() > numHosts) {
                lbHostUsageListRef.remove(0);
            }
        }
        return newMergedEventRecords;
    }

    public LoadBalancerMergedHostUsage initializeMergedRecord(LoadBalancerHostUsage lbHostUsage) {
        LoadBalancerMergedHostUsage newLBMergedHostUsage = new LoadBalancerMergedHostUsage();
        newLBMergedHostUsage.setAccountId(lbHostUsage.getAccountId());
        newLBMergedHostUsage.setLoadbalancerId(lbHostUsage.getLoadbalancerId());
        newLBMergedHostUsage.setNumVips(lbHostUsage.getNumVips());
        newLBMergedHostUsage.setEventType(lbHostUsage.getEventType());
        Calendar pollTime = Calendar.getInstance();
        pollTime.setTime(lbHostUsage.getPollTime().getTime());
        newLBMergedHostUsage.setPollTime(pollTime);
        newLBMergedHostUsage.setTagsBitmask(lbHostUsage.getTagsBitmask());
        return newLBMergedHostUsage;
    }

    public LoadBalancerHostUsage convertSnmpUsageToLBHostUsage(SnmpUsage snmpUsage, LoadBalancerHostUsage previousUsage, Calendar pollTime) {
        LoadBalancerHostUsage newlbHostUsage = new LoadBalancerHostUsage();
        newlbHostUsage.setAccountId(previousUsage.getAccountId());
        newlbHostUsage.setLoadbalancerId(previousUsage.getLoadbalancerId());
        newlbHostUsage.setTagsBitmask(previousUsage.getTagsBitmask());
        newlbHostUsage.setNumVips(previousUsage.getNumVips());
        newlbHostUsage.setPollTime(pollTime);
        newlbHostUsage.setHostId(previousUsage.getHostId());
        if  (snmpUsage != null) {
            newlbHostUsage.setOutgoingTransfer(snmpUsage.getBytesOut());
            newlbHostUsage.setOutgoingTransferSsl(snmpUsage.getBytesOutSsl());
            newlbHostUsage.setIncomingTransfer(snmpUsage.getBytesIn());
            newlbHostUsage.setIncomingTransferSsl(snmpUsage.getBytesInSsl());
            newlbHostUsage.setConcurrentConnections(snmpUsage.getConcurrentConnections());
            newlbHostUsage.setConcurrentConnectionsSsl(snmpUsage.getConcurrentConnectionsSsl());
        }

        return newlbHostUsage;
    }
}
