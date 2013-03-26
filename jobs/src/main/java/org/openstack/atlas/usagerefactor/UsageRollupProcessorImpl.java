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

        Usage newUsage = createInitializedUsageRecord(polledUsageRecordsForLb.get(0));
        newUsage.setStartTime(validHourToProcess);

        for(int i = 0; i < polledUsageRecordsForLb.size(); i++){
            BandwidthUsageHelper.calculateAndSetBandwidth(newUsage, polledUsageRecordsForLb.get(i));
            //If create lb event, set start time of the record to the poll time.
            if(polledUsageRecordsForLb.get(i).getEventType() != null){
                if(polledUsageRecordsForLb.get(i).getEventType().equals(UsageEvent.CREATE_LOADBALANCER.name())){
                    newUsage.setStartTime(polledUsageRecordsForLb.get(i).getPollTime());
                }
                //If delete lb event encountered, set end time to poll time.  May need to move bandwidth off this record and onto a previous record.
                if(polledUsageRecordsForLb.get(i).getEventType().equals(UsageEvent.DELETE_LOADBALANCER.name())){
                    newUsage.setEndTime(polledUsageRecordsForLb.get(i).getPollTime());
                    //if the delete event is the first record create a new NULL event record and put the bandwidth on that record.  Delete events should 0 bandwidth.
                    if(i == 0){

                    } else {

                    }
                }
            }

            if ((i + 1) >= polledUsageRecordsForLb.size()){
                break;
            }

            if (polledUsageRecordsForLb.get(i + 1).getEventType() != null &&
                !polledUsageRecordsForLb.get(i + 1).getEventType().toLowerCase().equals("null")){
                if(polledUsageRecordsForLb.get(i).getEventType().equals(UsageEvent.DELETE_LOADBALANCER.name())){
                    BandwidthUsageHelper.calculateAndSetBandwidth(newUsage, polledUsageRecordsForLb.get(i + 1));
                }
                newUsage.setEndTime(polledUsageRecordsForLb.get(i + 1).getPollTime());
                processedRecords.add(newUsage);
                newUsage = createInitializedUsageRecord(polledUsageRecordsForLb.get(i + 1));
            }
        }

        if(newUsage.getEndTime() == null){
            Calendar finalEndTime = new GregorianCalendar(validHourToProcess.get(Calendar.YEAR),
                    validHourToProcess.get(Calendar.MONTH), validHourToProcess.get(Calendar.DAY_OF_MONTH),
                    validHourToProcess.get(Calendar.HOUR), 0, 0);
            finalEndTime.add(Calendar.HOUR, 1);
            newUsage.setEndTime(finalEndTime);
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
}
