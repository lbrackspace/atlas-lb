package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.service.domain.usage.entities.PolledUsageRecord;

import java.util.List;

public interface PolledUsageRepository {
    List<PolledUsageRecord> getAllRecords(List<Integer> loadbalancerIds);
}
