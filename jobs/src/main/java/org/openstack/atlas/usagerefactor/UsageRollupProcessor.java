package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.service.domain.entities.Usage;

import java.util.List;
import java.util.Map;

public interface UsageRollupProcessor {
    Map<Integer, List<PolledUsageRecord>> breakDownUsagesByLbId(List<PolledUsageRecord> polledUsageRecords);

    List<Usage> processRecords(List<PolledUsageRecord> polledUsageRecords);
}
