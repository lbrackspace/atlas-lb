package org.openstack.atlas.service.domain.service.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.datamodel.CoreHealthMonitorType;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.datamodel.CoreProtocolType;
import org.openstack.atlas.service.domain.entity.HealthMonitor;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.*;
import org.openstack.atlas.service.domain.repository.HealthMonitorRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.HealthMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HealthMonitorServiceImpl implements HealthMonitorService {
    private final Log LOG = LogFactory.getLog(HealthMonitorServiceImpl.class);

    @Autowired
    protected LoadBalancerRepository loadBalancerRepository;
    @Autowired
    protected HealthMonitorRepository healthMonitorRepository;

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, ImmutableEntityException.class, UnprocessableEntityException.class})
    public HealthMonitor update(Integer loadBalancerId, HealthMonitor healthMonitor) throws PersistenceServiceException {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getById(loadBalancerId);
        HealthMonitor dbHealthMonitor = dbLoadBalancer.getHealthMonitor();
        HealthMonitor healthMonitorToUpdate = dbHealthMonitor == null ? healthMonitor : dbHealthMonitor;
        healthMonitorToUpdate.setLoadBalancer(dbLoadBalancer); // Needs to be set for hibernate

        verifyProtocol(healthMonitor, dbLoadBalancer);
        setPropertiesForUpdate(healthMonitor, dbHealthMonitor, healthMonitorToUpdate);

        loadBalancerRepository.changeStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), CoreLoadBalancerStatus.PENDING_UPDATE, false);
        dbLoadBalancer.setHealthMonitor(healthMonitorToUpdate);
        dbLoadBalancer = loadBalancerRepository.update(dbLoadBalancer);
        return dbLoadBalancer.getHealthMonitor();
    }

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class})
    public void preDelete(Integer loadBalancerId) throws PersistenceServiceException {
        if (healthMonitorRepository.getByLoadBalancerId(loadBalancerId) == null) throw new EntityNotFoundException("Health monitor not found");
    }

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class})
    public void delete(Integer loadBalancerId) throws PersistenceServiceException {
        healthMonitorRepository.delete(healthMonitorRepository.getByLoadBalancerId(loadBalancerId));
    }

    protected void verifyProtocol(final HealthMonitor requestMonitor, final LoadBalancer dbLoadBalancer) throws BadRequestException {
        if (requestMonitor.getType() != null) {
            if (requestMonitor.getType().equals(CoreHealthMonitorType.HTTP)) {
                if (!(dbLoadBalancer.getProtocol().equals(CoreProtocolType.HTTP))) {
                    throw new BadRequestException("Cannot update Health monitor to type HTTP because load balancer protocol is " + dbLoadBalancer.getProtocol());
                }
            } else if (requestMonitor.getType().equals(CoreHealthMonitorType.HTTPS)) {
                if (!(dbLoadBalancer.getProtocol().equals(CoreProtocolType.HTTPS))) {
                    throw new BadRequestException("Cannot update Health monitor to type HTTPS because load balancer protocol is " + dbLoadBalancer.getProtocol());
                }
            }
        } else {
            throw new BadRequestException("Must provide a type for the request");
        }
    }

    protected void setPropertiesForUpdate(final HealthMonitor healthMonitor, final HealthMonitor dbHealthMonitor, HealthMonitor healthMonitorToUpdate) throws BadRequestException {
        if (healthMonitor.getType().equals(CoreHealthMonitorType.CONNECT)) {
            if (healthMonitor.getPath() != null) {
                throw new BadRequestException("Updating to CONNECT monitor. Please provide all required fields.");
            }
            setConnectMonitorProperties(healthMonitor, dbHealthMonitor, healthMonitorToUpdate);
        } else {
            if ((healthMonitor.getType().equals(CoreHealthMonitorType.HTTP) || healthMonitor.getType().equals(CoreHealthMonitorType.HTTPS)) && healthMonitor.getPath() == null) {
                throw new BadRequestException("Updating to HTTP/HTTPS monitor. Please provide all required fields.");
            }
            setHttpMonitorProperties(healthMonitor, dbHealthMonitor, healthMonitorToUpdate);
        }
    }

    protected void setHttpMonitorProperties(final HealthMonitor requestMonitor, final HealthMonitor dbMonitor, HealthMonitor monitorToUpdate) throws BadRequestException {
        setConnectMonitorProperties(requestMonitor, dbMonitor, monitorToUpdate);

        if (requestMonitor.getPath() != null) monitorToUpdate.setPath(requestMonitor.getPath());
        else if (dbMonitor != null && dbMonitor.getPath() != null && dbMonitor.getPath().length() > 0)
            monitorToUpdate.setPath(dbMonitor.getPath());
        else throw new BadRequestException("Must provide a path for the request");
    }

    protected void setConnectMonitorProperties(final HealthMonitor requestMonitor, final HealthMonitor dbMonitor, HealthMonitor monitorToUpdate) throws BadRequestException {
        if (requestMonitor.getType() != null) monitorToUpdate.setType(requestMonitor.getType());
        else if (dbMonitor != null) monitorToUpdate.setType(dbMonitor.getType());
        else throw new BadRequestException("Must provide a type for the request");

        if (requestMonitor.getDelay() != null) monitorToUpdate.setDelay(requestMonitor.getDelay());
        else if (dbMonitor != null) monitorToUpdate.setDelay(dbMonitor.getDelay());
        else throw new BadRequestException("Must provide a delay for the request");

        if (requestMonitor.getTimeout() != null) monitorToUpdate.setTimeout(requestMonitor.getTimeout());
        else if (dbMonitor != null) monitorToUpdate.setTimeout(dbMonitor.getTimeout());
        else throw new BadRequestException("Must provide a timeout for the request");

        if (requestMonitor.getAttemptsBeforeDeactivation() != null)
            monitorToUpdate.setAttemptsBeforeDeactivation(requestMonitor.getAttemptsBeforeDeactivation());
        else if (dbMonitor != null)
            monitorToUpdate.setAttemptsBeforeDeactivation(dbMonitor.getAttemptsBeforeDeactivation());
        else throw new BadRequestException("Must provide attemptsBeforeActivation for the request");
    }
}
