package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.entity.HealthMonitor;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.*;

public interface HealthMonitorService {

    public HealthMonitor get(Integer accountId, Integer lbId) throws EntityNotFoundException, DeletedStatusException;

    public void update(LoadBalancer requestLb) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;

    public void prepareForDeletion(LoadBalancer requestLb) throws EntityNotFoundException, UnprocessableEntityException, ImmutableEntityException   ;

    public void delete(LoadBalancer requestLb) throws EntityNotFoundException;

     public void verifyMonitorProtocol(HealthMonitor queueMonitor, LoadBalancer dbLoadBalancer, HealthMonitor dbMonitor) throws BadRequestException;
}
