package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.service.domain.entities.Usage;

import java.util.ArrayList;
import java.util.List;

public class UsageRollupProcessorImpl implements UsageRollupProcessor {

    @Override
    public List<Usage> processRecords(List<PolledUsageRecord> polledUsageRecords) {
        if (polledUsageRecords == null || polledUsageRecords.isEmpty()){
            return new ArrayList<Usage>();
        }
        return null;
    }
}
