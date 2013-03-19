package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.service.domain.entities.Usage;

import java.util.List;

public interface UsageRollupProcessor {
    List<Usage> processRecords(List<PolledUsageRecord> polledUsageRecords);
}
