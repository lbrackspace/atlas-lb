package org.openstack.atlas.service.domain.services;


import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatusHistory;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;

public interface LoadBalancerStatusHistoryService {

    public LoadBalancerStatusHistory save(LoadBalancerStatusHistory loadBalancerStatusHistory);

    public LoadBalancerStatusHistory save(int accountId, int loadBalancerId, LoadBalancerStatus loadBalancerStatus);

    public void deleteLBStatusHistoryOlderThanSixMonths() throws EntityNotFoundException;

    }
