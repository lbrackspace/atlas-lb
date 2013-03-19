package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.service.domain.entities.Usage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsageRollupProcessorImpl implements UsageRollupProcessor {

    @Override
    public Map<Integer, List<PolledUsageRecord>> breakDownUsagesByLbId(List<PolledUsageRecord> polledUsageRecords) {
        Map<Integer, List<PolledUsageRecord>> usagesByLbId = new HashMap<Integer, List<PolledUsageRecord>>();
        for(PolledUsageRecord polledUsageRecord : polledUsageRecords){
            List<PolledUsageRecord> usageList;
            if (!usagesByLbId.containsKey(polledUsageRecord.getLoadbalancerId())){
                usageList = new ArrayList<PolledUsageRecord>();
                usagesByLbId.put(polledUsageRecord.getLoadbalancerId(), usageList);
            }
            usageList = usagesByLbId.get(polledUsageRecord.getLoadbalancerId());
            usageList.add(polledUsageRecord);
        }

        return usagesByLbId;
    }

    @Override
    public List<Usage> processRecords(List<PolledUsageRecord> polledUsageRecords) {
        List<Usage> processedRecords = new ArrayList<Usage>();

        if (polledUsageRecords == null || polledUsageRecords.isEmpty()){
            return processedRecords;
        }

        Map<Integer, List<PolledUsageRecord>> usagesByLbId = breakDownUsagesByLbId(polledUsageRecords);

        return processedRecords;
    }
}
