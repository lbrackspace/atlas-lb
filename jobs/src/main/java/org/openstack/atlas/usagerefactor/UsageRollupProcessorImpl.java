package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.service.domain.entities.Usage;

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

        for (PolledUsageRecord polledUsageRecord : polledUsageRecordsForLb) {
            // TODO: Implement
        }

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

}
