package org.openstack.atlas.jobs.usage;

import org.openstack.atlas.jobs.batch.BatchAction;
import org.openstack.atlas.service.domain.entity.UsageRecord;
import org.openstack.atlas.service.domain.repository.UsageRepository;

import java.util.List;

public class DbInsert implements BatchAction<UsageRecord> {
    private UsageRepository usageRepository;

    public DbInsert(UsageRepository usageRepository) {
        this.usageRepository = usageRepository;
    }

    @Override
    public void execute(List<UsageRecord> usageRecords) throws Exception {
        usageRepository.batchCreate(usageRecords);
    }
}
