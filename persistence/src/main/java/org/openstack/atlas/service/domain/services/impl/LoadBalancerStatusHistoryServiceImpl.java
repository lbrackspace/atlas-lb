package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatusHistory;
import org.openstack.atlas.service.domain.services.LoadBalancerStatusHistoryService;
import org.springframework.stereotype.Service;

import java.util.Calendar;

@Service
public class LoadBalancerStatusHistoryServiceImpl extends BaseService implements LoadBalancerStatusHistoryService {
    private final Log LOG = LogFactory.getLog(LoadBalancerStatusHistoryServiceImpl.class);

    @Override
    public LoadBalancerStatusHistory save(LoadBalancerStatusHistory loadBalancerStatusHistory) {
        return loadBalancerStatusHistoryRepository.save(loadBalancerStatusHistory);
    }

    @Override
    public LoadBalancerStatusHistory save(int accountId, int loadbalancerId, LoadBalancerStatus loadBalancerStatus) {
        LoadBalancerStatusHistory loadBalancerStatusHistory = new LoadBalancerStatusHistory();
        loadBalancerStatusHistory.setAccountId(accountId);
        loadBalancerStatusHistory.setLoadbalancerId(loadbalancerId);
        loadBalancerStatusHistory.setStatus(loadBalancerStatus);
        loadBalancerStatusHistory.setCreated(Calendar.getInstance());
        return loadBalancerStatusHistoryRepository.save(loadBalancerStatusHistory);
    }
}