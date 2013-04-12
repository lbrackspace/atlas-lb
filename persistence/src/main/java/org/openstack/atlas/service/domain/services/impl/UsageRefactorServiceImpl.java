package org.openstack.atlas.service.domain.services.impl;

import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;

public class UsageRefactorServiceImpl extends BaseService implements UsageRefactorService {

    @Override
    public void createUsageEvent(LoadBalancerHostUsage loadBalancerHostUsage) {
        hostUsageRefactorRepository.create(loadBalancerHostUsage);
    }

    @Override
    public LoadBalancerHostUsage getRecentHostUsageRecord(int lbId) {
        return hostUsageRefactorRepository.getMostRecentUsageRecordForLbId(lbId);
    }


}
