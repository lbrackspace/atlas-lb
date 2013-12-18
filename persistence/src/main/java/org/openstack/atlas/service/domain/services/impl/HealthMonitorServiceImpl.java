package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.services.HealthMonitorService;
import org.openstack.atlas.service.domain.services.LoadBalancerStatusHistoryService;
import org.openstack.atlas.service.domain.services.helpers.NodesPrioritiesContainer;
import org.openstack.atlas.service.domain.services.helpers.StringHelper;
import org.openstack.atlas.service.domain.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HealthMonitorServiceImpl extends BaseService implements HealthMonitorService {
    private final Log LOG = LogFactory.getLog(HealthMonitorServiceImpl.class);

    @Autowired
    private LoadBalancerStatusHistoryService loadBalancerStatusHistoryService;

    @Override
    public HealthMonitor get(Integer accountId, Integer lbId) throws EntityNotFoundException, DeletedStatusException {
        return loadBalancerRepository.getHealthMonitor(accountId, lbId);
    }

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, ImmutableEntityException.class, UnprocessableEntityException.class, BadRequestException.class})
    public void update(LoadBalancer requestLb) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException {
        LOG.debug("Entering " + getClass());
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(requestLb.getId(), requestLb.getAccountId());

        HealthMonitor requestMonitor = requestLb.getHealthMonitor();
        HealthMonitor dbMonitor = dbLoadBalancer.getHealthMonitor();
        HealthMonitor monitorToUpdate = dbMonitor == null ? new HealthMonitor() : dbMonitor;
        monitorToUpdate.setLoadbalancer(dbLoadBalancer); // Needs to be set

        verifyMonitorProtocol(requestMonitor, dbLoadBalancer, dbMonitor);

        if (requestMonitor.getType().equals(HealthMonitorType.CONNECT)) {
            if (requestMonitor.getPath() != null || requestMonitor.getStatusRegex() != null || requestMonitor.getBodyRegex() != null) {
                throw new BadRequestException("Updating to CONNECT monitor. Please provide the required fields only.");
            }
            setConnectMonitorProperties(requestMonitor, dbMonitor, monitorToUpdate);
        } else {
            if ((requestMonitor.getType().equals(HealthMonitorType.HTTP) || requestMonitor.getType().equals(HealthMonitorType.HTTPS)) && requestMonitor.getPath() == null) {
                throw new BadRequestException("Updating to HTTP/HTTPS monitor. Please provide all required fields.");
            }
            setHttpMonitorProperties(requestMonitor, dbMonitor, monitorToUpdate);
        }

        LOG.debug("Updating the lb status to pending_update");
        if (!loadBalancerRepository.testAndSetStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE, false)) {
            String message = StringHelper.immutableLoadBalancer(dbLoadBalancer);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        } else {
            //Set status record
            loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE);
        }

        dbLoadBalancer.setHealthMonitor(monitorToUpdate);
        loadBalancerRepository.update(dbLoadBalancer);

        LOG.debug("Leaving " + getClass());
    }

    @Override
    public void verifyMonitorProtocol(HealthMonitor requestMonitor, LoadBalancer dbLoadBalancer, HealthMonitor dbMonitor) throws BadRequestException {
        if (requestMonitor.getType() != null) {
            if (dbLoadBalancer.getProtocol().equals(LoadBalancerProtocol.DNS_UDP) || dbLoadBalancer.getProtocol().equals(LoadBalancerProtocol.UDP) || dbLoadBalancer.getProtocol().equals(LoadBalancerProtocol.UDP_STREAM)) {
                throw new BadRequestException("Protocol UDP, UDP_STREAM and DNS_UDP are not allowed with health monitors. ");
            }
            if (requestMonitor.getType().equals(HealthMonitorType.HTTP)) {
                if (!(dbLoadBalancer.getProtocol().name().equals(LoadBalancerProtocol.HTTP.name()))) {
                    throw new BadRequestException("Cannot update Health Monitor to Type HTTP because load balancer protocol is " + dbLoadBalancer.getProtocol().name());
                }
            } else if (requestMonitor.getType().equals(HealthMonitorType.HTTPS)) {
                if (!(dbLoadBalancer.getProtocol().name().equals(LoadBalancerProtocol.HTTPS.name()))) {
                    throw new BadRequestException("Cannot update Health Monitor to Type HTTPS because load balancer protocol is " + dbLoadBalancer.getProtocol().name());
                }
            }
        } else {
            throw new BadRequestException("Must provide a type for the request");
        }
    }

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, ImmutableEntityException.class, UnprocessableEntityException.class})
    public void prepareForDeletion(LoadBalancer requestLb) throws EntityNotFoundException, UnprocessableEntityException, ImmutableEntityException, BadRequestException {
        LOG.debug("Entering " + getClass());
        LoadBalancer dbLb = loadBalancerRepository.getByIdAndAccountId(requestLb.getId(), requestLb.getAccountId());
        isLbActive(dbLb);

        // we won't allowed to delete a health monitor if it has Secondary nodes
        NodesPrioritiesContainer npc = new NodesPrioritiesContainer(dbLb.getNodes());
        if (npc.hasSecondary()) {
                throw new BadRequestException(Constants.WontDeleteMonitorCauseSecNodes);
        }

        if (dbLb.getHealthMonitor() == null) {
            throw new UnprocessableEntityException("No health monitor found to delete.");
        }

        LOG.debug("Updating the lb status to pending_update");
        if (!loadBalancerRepository.testAndSetStatus(dbLb.getAccountId(), dbLb.getId(), LoadBalancerStatus.PENDING_UPDATE, false)) {
            String message = StringHelper.immutableLoadBalancer(dbLb);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        } else {
            //Set status record
            loadBalancerStatusHistoryService.save(dbLb.getAccountId(), dbLb.getId(), LoadBalancerStatus.PENDING_UPDATE);
        }
    }

    @Override
    @Transactional
    public void delete(LoadBalancer requestLb) throws EntityNotFoundException, Exception {
        LoadBalancer dbLb = loadBalancerRepository.getByIdAndAccountId(requestLb.getId(), requestLb.getAccountId());
        for (Node node : dbLb.getNodes()) {
            if (node.getCondition().equals(NodeCondition.ENABLED))
                node.setStatus(NodeStatus.ONLINE);
        }
        loadBalancerRepository.update(dbLb);
        loadBalancerRepository.removeHealthMonitor(dbLb);
    }

    private void setHttpMonitorProperties(HealthMonitor requestMonitor, HealthMonitor dbMonitor, HealthMonitor newMonitor) throws BadRequestException {
        setConnectMonitorProperties(requestMonitor, dbMonitor, newMonitor);

        if (requestMonitor.getPath() != null) {
            newMonitor.setPath(requestMonitor.getPath());
        } else if (dbMonitor != null && dbMonitor.getPath() != null && dbMonitor.getPath().length() > 0) {
            newMonitor.setPath(dbMonitor.getPath());
        } else {
            throw new BadRequestException("Must provide a path for the request");
        }

        if (requestMonitor.getStatusRegex() != null) {
            newMonitor.setStatusRegex(requestMonitor.getStatusRegex());
        } else if (dbMonitor != null && dbMonitor.getStatusRegex() != null && dbMonitor.getStatusRegex().length() > 0) {
            newMonitor.setStatusRegex(dbMonitor.getStatusRegex());
        } else {
            newMonitor.setStatusRegex(null);
        }

        if (requestMonitor.getBodyRegex() != null) {
            newMonitor.setBodyRegex(requestMonitor.getBodyRegex());
        } else if (dbMonitor != null && dbMonitor.getBodyRegex() != null && dbMonitor.getBodyRegex().length() > 0) {
            newMonitor.setBodyRegex(dbMonitor.getBodyRegex());
        } else {
            newMonitor.setBodyRegex(null);
        }

        if (requestMonitor.getHostHeader() != null) {
            newMonitor.setHostHeader(requestMonitor.getHostHeader());
        } else if (dbMonitor != null && dbMonitor.getHostHeader() != null) {
            newMonitor.setHostHeader(dbMonitor.getHostHeader());
        } else {
            newMonitor.setHostHeader(null);
        }
    }

    private void setConnectMonitorProperties(HealthMonitor requestMonitor, HealthMonitor dbMonitor, HealthMonitor newMonitor) throws BadRequestException {
        if (requestMonitor.getType() != null) {
            newMonitor.setType(requestMonitor.getType());
        } else if (dbMonitor != null) {
            newMonitor.setType(dbMonitor.getType());
        } else {
            throw new BadRequestException("Must provide a type for the request");
        }

        if (requestMonitor.getDelay() != null) {
            newMonitor.setDelay(requestMonitor.getDelay());
        } else if (dbMonitor != null) {
            newMonitor.setDelay(dbMonitor.getDelay());
        } else {
            throw new BadRequestException("Must provide a delay for the request");
        }

        if (requestMonitor.getTimeout() != null) {
            newMonitor.setTimeout(requestMonitor.getTimeout());
        } else if (dbMonitor != null) {
            newMonitor.setTimeout(dbMonitor.getTimeout());
        } else {
            throw new BadRequestException("Must provide a timeout for the request");
        }

        if (requestMonitor.getAttemptsBeforeDeactivation() != null) {
            newMonitor.setAttemptsBeforeDeactivation(requestMonitor.getAttemptsBeforeDeactivation());
        } else if (dbMonitor != null) {
            newMonitor.setAttemptsBeforeDeactivation(dbMonitor.getAttemptsBeforeDeactivation());
        } else {
            throw new BadRequestException("Must provide attemptsBeforeActivation for the request");
        }

        newMonitor.setPath(null);
        newMonitor.setStatusRegex(null);
        newMonitor.setBodyRegex(null);
    }
}
