package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.usagerefactor.helpers.BandwidthUsageHelper;

import java.util.*;

public class UsageRollupProcessorImpl implements UsageRollupProcessor {

    @Override
    public Map<Integer, List<PolledUsageRecord>> breakDownUsagesByLbId(List<PolledUsageRecord> polledUsageRecords) {
        Map<Integer, List<PolledUsageRecord>> usagesByLbId = new HashMap<Integer, List<PolledUsageRecord>>();

        for (PolledUsageRecord polledUsageRecord : polledUsageRecords) {
            List<PolledUsageRecord> usageList;

            if (!usagesByLbId.containsKey(polledUsageRecord.getLoadbalancerId())) {
                usageList = new ArrayList<PolledUsageRecord>();
                usagesByLbId.put(polledUsageRecord.getLoadbalancerId(), usageList);
            }

            usageList = usagesByLbId.get(polledUsageRecord.getLoadbalancerId());
            usageList.add(polledUsageRecord);
        }

        return usagesByLbId;
    }

    @Override
    public List<Usage> processRecords(List<PolledUsageRecord> polledUsageRecords, Calendar hourToProcess) {
        List<Usage> processedRecords = new ArrayList<Usage>();

        if (polledUsageRecords == null || polledUsageRecords.isEmpty()) {
            return processedRecords;
        }


        Map<Integer, List<PolledUsageRecord>> usagesByLbId = breakDownUsagesByLbId(polledUsageRecords);

        for (Integer lbId : usagesByLbId.keySet()) {
            List<PolledUsageRecord> polledRecordsForLb = usagesByLbId.get(lbId);

            List<Usage> processedRecordsForLb = processRecordsForLb(polledRecordsForLb, hourToProcess);
            processedRecords.addAll(processedRecordsForLb);
        }

        return processedRecords;
    }

    @Override
    public List<Usage> processRecordsForLb(List<PolledUsageRecord> polledUsageRecordsForLb, Calendar hourToProcess) {
        List<Usage> processedRecords = new ArrayList<Usage>();

        if (polledUsageRecordsForLb == null || polledUsageRecordsForLb.isEmpty()) {
            return processedRecords;
        }
        Calendar validHourToProcess = stripOutMinsAndSecs(hourToProcess);
        Calendar hourToStopProcess = stripOutMinsAndSecs(validHourToProcess);
        hourToStopProcess.add(Calendar.HOUR, 1);

        Usage newUsage = createInitializedUsageRecord(polledUsageRecordsForLb.get(0));
        newUsage.setStartTime(validHourToProcess);

        for(int i = 0; i < polledUsageRecordsForLb.size(); i++){
            if(polledUsageRecordsForLb.get(i).getPollTime().compareTo(validHourToProcess) < 0){
                continue;
            }
            if(polledUsageRecordsForLb.get(i).getPollTime().compareTo(hourToStopProcess) >= 0){
                break;
            }
            BandwidthUsageHelper.calculateAndSetBandwidth(newUsage, polledUsageRecordsForLb.get(i));
            processEvents(newUsage, polledUsageRecordsForLb.get(i), processedRecords);
        }

        if(newUsage.getEndTime() == null){
            newUsage.setEndTime(hourToStopProcess);
        }

        processedRecords.add(newUsage);
        return processedRecords;
    }

    private Calendar stripOutMinsAndSecs(Calendar cal) {
        Calendar newCal = Calendar.getInstance();
        newCal.setTime(cal.getTime());
        newCal.set(Calendar.MINUTE, 0);
        newCal.set(Calendar.SECOND, 0);
        newCal.set(Calendar.MILLISECOND, 0);
        return newCal;
    }

    private Usage createInitializedUsageRecord(PolledUsageRecord polledUsageRecord){
        LoadBalancer currentLB = new LoadBalancer();
        currentLB.setId(polledUsageRecord.getLoadbalancerId());
        currentLB.setAccountId(polledUsageRecord.getAccountId());
        Usage initUsage =  new Usage();
        initUsage.setLoadbalancer(currentLB);
        initUsage.setStartTime(polledUsageRecord.getPollTime());
        initUsage.setEventType(polledUsageRecord.getEventType());
        initUsage.setAccountId(polledUsageRecord.getAccountId());
        initUsage.setNeedsPushed(true);
        initUsage.setEntryVersion(0);
        return initUsage;
    }

    private void processEvents(Usage currentUsage, PolledUsageRecord currentPolledRecord, List<Usage> processedRecords){
                    //TODO: Handle events with same poll time (Event happened for lb with multiple IPs and/or has ssl term)
            if(currentPolledRecord.getEventType() != null  &&
                !currentPolledRecord.getEventType().toLowerCase().equals("null")){

                //If first record is the CREATE_LB event only set the start time of the processed usage
                if(!currentPolledRecord.getEventType().equals(UsageEvent.CREATE_LOADBALANCER.name())){
                    currentUsage.setEndTime(currentPolledRecord.getPollTime());
                    currentUsage.setEventType(null);
                    processedRecords.add(currentUsage);
                    currentUsage = createInitializedUsageRecord(currentPolledRecord);
                } else {
                    currentUsage.setStartTime(currentPolledRecord.getPollTime()); 
                }
                //If delete lb event encountered, set end time to poll time.  May need to move bandwidth off this record and onto a previous record.
                if(currentPolledRecord.getEventType().equals(UsageEvent.DELETE_LOADBALANCER.name())){
                    currentUsage.setEndTime(currentPolledRecord.getPollTime());
                }
                if(currentPolledRecord.getEventType().equals(UsageEvent.CREATE_VIRTUAL_IP.name())){

                }
                if(currentPolledRecord.getEventType().equals(UsageEvent.DELETE_VIRTUAL_IP.name())){

                }
                if(currentPolledRecord.getEventType().equals(UsageEvent.SSL_MIXED_ON.name())){

                }
                if(currentPolledRecord.getEventType().equals(UsageEvent.SSL_ONLY_ON.name())){

                }
                if(currentPolledRecord.getEventType().equals(UsageEvent.SSL_OFF.name())){

                }
                if(currentPolledRecord.getEventType().equals(UsageEvent.SUSPEND_LOADBALANCER.name())){

                }
                if(currentPolledRecord.getEventType().equals(UsageEvent.UNSUSPEND_LOADBALANCER.name())){

                }
                if(currentPolledRecord.getEventType().equals(UsageEvent.SUSPENDED_LOADBALANCER.name())){

                }
            }
    }
}
