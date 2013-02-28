package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.HealthMonitor;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.*;

public interface HealthMonitorService {

    public HealthMonitor get(Integer accountId, Integer lbId) throws EntityNotFoundException, DeletedStatusException;

    public void update(LoadBalancer requestLb) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;

    public void prepareForDeletion(LoadBalancer requestLb) throws EntityNotFoundException, UnprocessableEntityException, ImmutableEntityException, BadRequestException;

    public void delete(LoadBalancer requestLb) throws EntityNotFoundException, Exception;

     public void verifyMonitorProtocol(HealthMonitor queueMonitor, LoadBalancer dbLoadBalancer, HealthMonitor dbMonitor) throws BadRequestException;
}
