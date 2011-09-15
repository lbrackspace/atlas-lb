package org.openstack.atlas.service.domain.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.common.StringHelper;
import org.openstack.atlas.service.domain.common.StringUtilities;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancerProtocol;
import org.openstack.atlas.service.domain.entity.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entity.SessionPersistence;
import org.openstack.atlas.service.domain.exception.*;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
public class LoadBalancerServiceImpl implements LoadBalancerService {
    private final Log LOG = LogFactory.getLog(LoadBalancerServiceImpl.class);

    @Autowired
    protected AccountLimitService accountLimitService;

    @Autowired
    protected BlacklistService blacklistService;

    @Autowired
    protected HostService hostService;

    @Autowired
    protected LoadBalancerRepository loadBalancerRepository;

    @Autowired
    protected VirtualIpService virtualIpService;

    @Override
    @Transactional
    public final LoadBalancer create(LoadBalancer loadBalancer) throws PersistenceServiceException {
        validate(loadBalancer);
        loadBalancer = addDefaultValues(loadBalancer);
        LoadBalancer dbLoadBalancer = loadBalancerRepository.create(loadBalancer);
        dbLoadBalancer.setUserName(loadBalancer.getUserName());
        return dbLoadBalancer;
    }

    @Override
    @Transactional
    public LoadBalancer update(LoadBalancer loadBalancer) throws PersistenceServiceException {
        LoadBalancer dbLoadBalancer;
        boolean portHMTypecheck = true;

        dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(loadBalancer.getId(), loadBalancer.getAccountId());

        LOG.debug("Updating the lb status to pending_update");
        if(!loadBalancerRepository.testAndSetStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE, false)) {
            String message = StringHelper.immutableLoadBalancer(dbLoadBalancer);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        }

        if (loadBalancer.getPort() != null && !loadBalancer.getPort().equals(dbLoadBalancer.getPort())) {
            LOG.debug("Updating loadbalancer port to " + loadBalancer.getPort());
            if (loadBalancerRepository.canUpdateToNewPort(loadBalancer.getPort(), dbLoadBalancer.getLoadBalancerJoinVipSet())) {
                loadBalancerRepository.updatePortInJoinTable(loadBalancer);
                dbLoadBalancer.setPort(loadBalancer.getPort());
            } else {
                LOG.error("Cannot update load balancer port as it is currently in use by another virtual ip.");
                throw new BadRequestException(String.format("Port currently assigned to one of the virtual ips. Please try another port."));
            }
        }

        if (loadBalancer.getName() != null && !loadBalancer.getName().equals(dbLoadBalancer.getName())) {
            LOG.debug("Updating loadbalancer name to " + loadBalancer.getName());
            dbLoadBalancer.setName(loadBalancer.getName());
        }

        if (loadBalancer.getAlgorithm() != null && !loadBalancer.getAlgorithm().equals(dbLoadBalancer.getAlgorithm())) {
            LOG.debug("Updating loadbalancer algorithm to " + loadBalancer.getAlgorithm());
            dbLoadBalancer.setAlgorithm(loadBalancer.getAlgorithm());
        }

        if (loadBalancer.getProtocol() != null && !loadBalancer.getProtocol().equals(dbLoadBalancer.getProtocol())) {

            //check for health monitor type and allow update only if protocol matches health monitory type for HTTP and HTTPS
            if (dbLoadBalancer.getHealthMonitor() != null) {
                if (dbLoadBalancer.getHealthMonitor().getType() != null) {
                    if (dbLoadBalancer.getHealthMonitor().getType().name().equals(LoadBalancerProtocol.HTTP.name())) {
                        //incoming port not HTTP
                        if (!(loadBalancer.getProtocol().name().equals(LoadBalancerProtocol.HTTP.name()))) {
                            portHMTypecheck = false;
                        }
                    } else if (dbLoadBalancer.getHealthMonitor().getType().name().equals(LoadBalancerProtocol.HTTPS.name())) {
                        //incoming port not HTTP
                        if (!(loadBalancer.getProtocol().name().equals(LoadBalancerProtocol.HTTPS.name()))) {
                            portHMTypecheck = false;
                        }
                    }
                }
            }

            if (portHMTypecheck) {
                /* Notify the Usage Processor on changes of protocol to and from secure protocols */
                //notifyUsageProcessorOfSslChanges(message, queueLb, dbLoadBalancer);

                if (loadBalancer.getProtocol().equals(LoadBalancerProtocol.HTTP)) {
                    LOG.debug("Updating loadbalancer protocol to " + loadBalancer.getProtocol());
                    dbLoadBalancer.setProtocol(loadBalancer.getProtocol());
                } else {
                    dbLoadBalancer.setSessionPersistence(SessionPersistence.NONE);
                    dbLoadBalancer.setProtocol(loadBalancer.getProtocol());
                }
            } else {
                LOG.error("Cannot update port as the loadbalancer has a incompatible Health Monitor type");
                throw new BadRequestException(String.format("Cannot update port as the loadbalancer has a incompatible Health Monitor type"));
            }
        }

        if (loadBalancer.getConnectionLogging() != null && !loadBalancer.getConnectionLogging().equals(dbLoadBalancer.getConnectionLogging())) {
            if (loadBalancer.getConnectionLogging()) {
                if (loadBalancer.getProtocol() != LoadBalancerProtocol.HTTP) {
                    LOG.error("Protocol must be HTTP for connection logging.");
                    throw new UnprocessableEntityException(String.format("Protocol must be HTTP for connection logging."));
                }
                LOG.debug("Enabling connection logging on the loadbalancer...");
            } else {
                LOG.debug("Disabling connection logging on the loadbalancer...");
            }
            dbLoadBalancer.setConnectionLogging(loadBalancer.getConnectionLogging());
        }

        dbLoadBalancer = loadBalancerRepository.update(dbLoadBalancer);
        dbLoadBalancer.setUserName(loadBalancer.getUserName());
        LOG.debug("Updated the loadbalancer in DB. Now sending response back.");

        // TODO: Sending db loadbalancer causes everything to update. Tweek for performance
        LOG.debug("Leaving " + getClass());
        return dbLoadBalancer;
    }

    @Override
    @Transactional
    public void delete(LoadBalancer lb) throws PersistenceServiceException {
        List<Integer> loadBalancerIds = new ArrayList<Integer>();
        loadBalancerIds.add(lb.getId());
        delete(lb.getAccountId(), loadBalancerIds);
    }

    @Override
    @Transactional
    public void delete(Integer accountId, List<Integer> loadBalancerIds) throws PersistenceServiceException {
        List<Integer> badLbIds = new ArrayList<Integer>();
        List<Integer> badLbStatusIds = new ArrayList<Integer>();
        List<LoadBalancer> loadBalancersToDelete = new ArrayList<LoadBalancer>();
        for (int lbIdToDelete : loadBalancerIds) {
            try {
                LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(lbIdToDelete, accountId);
                if(!loadBalancerRepository.testAndSetStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_DELETE, false)) {
                    LOG.warn(StringHelper.immutableLoadBalancer(dbLoadBalancer));
                    badLbStatusIds.add(lbIdToDelete);
                }
                loadBalancersToDelete.add(dbLoadBalancer);
            } catch (Exception e) {
                badLbIds.add(lbIdToDelete);
            }
        }
        if (!badLbIds.isEmpty()) throw new BadRequestException(String.format("Must provide valid load balancers, %s , could not be found.", StringUtilities.DelimitString(badLbIds, ",")));
        if (!badLbStatusIds.isEmpty()) throw new BadRequestException(String.format("Must provide valid load balancers, %s , are immutable and could not be processed.", StringUtilities.DelimitString(badLbStatusIds, ",")));
    }

    @Transactional
    public void pseudoDelete(LoadBalancer lb) throws Exception {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(lb.getId(), lb.getAccountId());
        dbLoadBalancer.setStatus(LoadBalancerStatus.DELETED);
        dbLoadBalancer = loadBalancerRepository.update(dbLoadBalancer);

        virtualIpService.removeAllVipsFromLoadBalancer(dbLoadBalancer);
    }

    protected void validate(LoadBalancer loadBalancer) throws BadRequestException, EntityNotFoundException, LimitReachedException {
        Validator.verifyTCPProtocolandPort(loadBalancer);
        Validator.verifyProtocolAndHealthMonitorType(loadBalancer);
        accountLimitService.verifyLoadBalancerLimit(loadBalancer.getAccountId());
        blacklistService.verifyNoBlacklistNodes(loadBalancer.getNodes());
    }

    protected LoadBalancer addDefaultValues(LoadBalancer loadBalancer) throws PersistenceServiceException {
        LoadBalancerDefaultBuilder.addDefaultValues(loadBalancer);
        loadBalancer.setHost(hostService.getDefaultActiveHost());
        loadBalancer = virtualIpService.assignVIpsToLoadBalancer(loadBalancer);
        return loadBalancer;
    }
}

