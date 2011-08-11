package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.entity.HealthMonitor;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.*;
import org.springframework.stereotype.Service;

@Service
public class NullHealthMonitorService implements HealthMonitorService {
    @Override
    public HealthMonitor get(Integer accountId, Integer lbId) throws EntityNotFoundException, DeletedStatusException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void update(LoadBalancer requestLb) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void prepareForDeletion(LoadBalancer requestLb) throws EntityNotFoundException, UnprocessableEntityException, ImmutableEntityException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void delete(LoadBalancer requestLb) throws EntityNotFoundException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void verifyMonitorProtocol(HealthMonitor queueMonitor, LoadBalancer dbLoadBalancer, HealthMonitor dbMonitor) throws BadRequestException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
