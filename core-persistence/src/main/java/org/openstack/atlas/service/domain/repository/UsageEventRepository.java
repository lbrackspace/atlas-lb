package org.openstack.atlas.service.domain.repository;

import org.openstack.atlas.service.domain.entity.UsageEventRecord;

import java.util.List;

public interface UsageEventRepository {
    List<UsageEventRecord> getAllUsageEventEntries();

    void batchCreate(List<UsageEventRecord> usages);

    void batchDelete(List<UsageEventRecord> usages);
}
