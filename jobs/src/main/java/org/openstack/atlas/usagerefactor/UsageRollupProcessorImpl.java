package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.usage.entities.PolledUsageRecord;
import org.openstack.atlas.usagerefactor.helpers.RollupUsageHelper;

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
            RollupUsageHelper.calculateAndSetBandwidth(newUsage, polledUsageRecordsForLb.get(i));
            RollupUsageHelper.calculateAndSetAverageConcurrentConnections(newUsage, polledUsageRecordsForLb.get(i));
            newUsage = processEvents(newUsage, polledUsageRecordsForLb.get(i), processedRecords, i == 0);
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
        if(polledUsageRecord.getEventType() != null){
            initUsage.setEventType(polledUsageRecord.getEventType().name());
        }
        initUsage.setAccountId(polledUsageRecord.getAccountId());
        initUsage.setTags(polledUsageRecord.getTagsBitmask());
        initUsage.setNeedsPushed(true);
        initUsage.setEntryVersion(0);
        return initUsage;
    }

    private Usage processEvents(Usage currentUsage, PolledUsageRecord currentPolledRecord, List<Usage> processedRecords, boolean isFirstOfHour){
            if(currentPolledRecord.getEventType() != null){
                if(currentPolledRecord.getEventType() != UsageEvent.CREATE_LOADBALANCER){
                    currentUsage.setEndTime(currentPolledRecord.getPollTime());
                    if(isFirstOfHour){
                        currentUsage.setEventType(null);
                    }
                    processedRecords.add(currentUsage);
                    currentUsage = createInitializedUsageRecord(currentPolledRecord);
                }
                switch(currentPolledRecord.getEventType()){
                    case CREATE_LOADBALANCER:
                        currentUsage.setStartTime(currentPolledRecord.getPollTime());
                        break;
                    case DELETE_LOADBALANCER:
                        currentUsage.setEndTime(currentPolledRecord.getPollTime());
                        break;
                    case CREATE_VIRTUAL_IP:
                        currentUsage.setNumVips(currentUsage.getNumVips() + 1);
                        break;
                    case DELETE_VIRTUAL_IP:
                        currentUsage.setNumVips(currentUsage.getNumVips() - 1);
                        break;
                    case SSL_MIXED_ON:
                        currentUsage.setTags(currentPolledRecord.getTagsBitmask());
                        break;
                    case SSL_ONLY_ON:
                        currentUsage.setTags(currentPolledRecord.getTagsBitmask());
                        break;
                    case SSL_OFF:
                        currentUsage.setTags(currentPolledRecord.getTagsBitmask());
                        break;
                    case SSL_ON:
                        currentUsage.setTags(currentPolledRecord.getTagsBitmask());
                        break;
                    case SUSPEND_LOADBALANCER:
                        break;
                    case UNSUSPEND_LOADBALANCER:
                        break;
                    case SUSPENDED_LOADBALANCER:
                        break;
                    default:
                        break;
                }
            }
        return currentUsage;
    }
}
