package org.openstack.atlas.service.domain.repository;

import org.openstack.atlas.service.domain.entity.UsageRecord;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;

import java.util.List;
import java.util.Set;

public interface UsageRepository {
    List<UsageRecord> getByLoadBalancerId(Integer loadBalancerId) throws EntityNotFoundException;

    List<UsageRecord> getMostRecentUsageRecordsForLoadBalancers(Set<Integer> lbIds);

    void batchCreate(List<UsageRecord> recordsToInsert);

    void batchUpdate(List<UsageRecord> recordsToUpdate);
}
