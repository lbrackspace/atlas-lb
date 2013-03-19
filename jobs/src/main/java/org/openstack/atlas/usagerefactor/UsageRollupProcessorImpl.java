package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.service.domain.entities.Usage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsageRollupProcessorImpl implements UsageRollupProcessor {

    @Override
    public Map<Integer, List<Usage>> breakDownUsagesByLbId(List<PolledUsageRecord> polledUsageRecords) {
        Map<Integer, List<Usage>> usagesByLbId = new HashMap<Integer, List<Usage>>();

        return usagesByLbId;
    }

    @Override
    public List<Usage> processRecords(List<PolledUsageRecord> polledUsageRecords) {
        List<Usage> processedRecords = new ArrayList<Usage>();

        if (polledUsageRecords == null || polledUsageRecords.isEmpty()){
            return processedRecords;
        }

        Map<Integer, List<Usage>> usagesByLbId = breakDownUsagesByLbId(polledUsageRecords);

        return processedRecords;
    }
}
