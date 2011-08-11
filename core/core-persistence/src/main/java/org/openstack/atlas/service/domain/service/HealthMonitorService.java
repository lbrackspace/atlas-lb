package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.entity.HealthMonitor;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.*;

public interface HealthMonitorService {

    HealthMonitor get(Integer accountId, Integer lbId) throws EntityNotFoundException, DeletedStatusException;

    void update(LoadBalancer requestLb) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;

    void prepareForDeletion(LoadBalancer requestLb) throws EntityNotFoundException, UnprocessableEntityException, ImmutableEntityException   ;

    void delete(LoadBalancer requestLb) throws EntityNotFoundException;

    void verifyMonitorProtocol(HealthMonitor queueMonitor, LoadBalancer dbLoadBalancer, HealthMonitor dbMonitor) throws BadRequestException;
}
