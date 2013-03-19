package org.openstack.atlas.usagerefactor;

import java.util.List;

public interface PolledUsageRepository {
    List<PolledUsageRecord> getAllRecords(List<Integer> loadbalancerIds);
}
