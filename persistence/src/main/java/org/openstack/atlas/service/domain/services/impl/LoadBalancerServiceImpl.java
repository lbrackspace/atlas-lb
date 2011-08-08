package org.openstack.atlas.service.domain.services.impl;

import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.HostStatus;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Suspension;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.pojos.AccountBilling;
import org.openstack.atlas.service.domain.pojos.LbQueryStatus;
import org.openstack.atlas.service.domain.services.*;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.openstack.atlas.service.domain.services.helpers.NodesHelper;
import org.openstack.atlas.service.domain.services.helpers.StringHelper;
import org.openstack.atlas.service.domain.util.Constants;
import org.openstack.atlas.service.domain.util.StringUtilities;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.openstack.atlas.util.ip.exception.IpTypeMissMatchException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.HTTP;
import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.*;

@Service
public class LoadBalancerServiceImpl extends BaseService implements LoadBalancerService {
    private final Log LOG = LogFactory.getLog(LoadBalancerServiceImpl.class);
    private NotificationService notificationService;
    private AccountLimitService accountLimitService;
    private VirtualIpService virtualIpService;
    private HostService hostService;

    @Required
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Required
    public void setVirtualIpService(VirtualIpService virtualIpService) {
        this.virtualIpService = virtualIpService;
    }

    @Required
    public void setAccountLimitService(AccountLimitService accountLimitService) {
        this.accountLimitService = accountLimitService;
    }

    @Required
    public void setHostService(HostService hostService) {
        this.hostService = hostService;
    }

    @Override
    @Transactional
    public LoadBalancer create(LoadBalancer lb) throws Exception {
        if (isLoadBalancerLimitReached(lb.getAccountId())) {
            LOG.error("Load balancer limit reached. Sending error response to client...");
            throw new LimitReachedException(String.format("Load balancer limit reached. "
                    + "Limit is set to '%d'. Contact support if you would like to increase your limit.",
                    getLoadBalancerLimit(lb.getAccountId())));
        }

        //check for blacklisted Nodes
         try {
            Node badNode = blackListedItemNode(lb.getNodes());
            if (badNode != null) {
                throw new BadRequestException(String.format("Invalid node address. The address '%s' is currently not accepted for this request.", badNode.getIpAddress()));
            }
        } catch (IPStringConversionException ipe) {
            LOG.warn("IPStringConversionException thrown. Sending error response to client...");
            throw new BadRequestException("IP address was not converted properly, we are unable to process this request.");
        } catch (IpTypeMissMatchException ipte) {
            LOG.warn("EntityNotFoundException thrown. Sending error response to client...");
            throw new BadRequestException("IP addresses type are mismatched, we are unable to process this request.");
        }

        try {
            //check for TCP protocol and port before adding default, since TCP protocol has no default
            verifyTCPProtocolandPort(lb);
            addDefaultValues(lb);
            verifySessionPersistence(lb);
            verifyProtocolAndHealthMonitorType(lb);
            setHostForNewLoadBalancer(lb);
            setVipConfigForLoadBalancer(lb);
        } catch (UniqueLbPortViolationException e) {
            LOG.debug("The port of the new LB is the same as the LB to which you wish to share a virtual ip.");
            throw e;
        } catch (AccountMismatchException e) {
            LOG.debug("The accounts do not match for the requested shared virtual ip.");
            throw e;
        } catch (BadRequestException e) {
            LOG.debug("Protocol must be HTTP for session persistence.");
            throw e;
        } catch (ProtocolHealthMonitorMismatchException e) {
            LOG.debug("Protocol type of HTTP/HTTPS must match Health Monitor Type of HTTP/HTTPS.");
            throw e;
        } catch (TCPProtocolUnknownPortException e) {
            LOG.debug("Port must be supplied for TCP Protocol.");
            throw e;
        } catch (UnprocessableEntityException e) {
            LOG.debug("There is an error regarding the hosts, with a shared vip the LB's must reside on the same Host.");
            throw e;
        } catch (OutOfVipsException e) {
            LOG.error("Out of virtual ips! Sending error response to client...");
            String errorMessage = e.getMessage();
            notificationService.saveAlert(lb.getAccountId(), lb.getId(), e, AlertType.API_FAILURE.name(), errorMessage);
            throw e;
        } catch (IllegalArgumentException e) {
            LOG.error("Virtual Ip could not be processed....");
            String errorMessage = e.getMessage();
            notificationService.saveAlert(lb.getAccountId(), lb.getId(), e, AlertType.API_FAILURE.name(), errorMessage);
            throw e;
        }

        LoadBalancer dbLoadBalancer = loadBalancerRepository.create(lb);
        dbLoadBalancer.setUserName(lb.getUserName());
        joinIpv6OnLoadBalancer(dbLoadBalancer);
        return dbLoadBalancer;
    }

    @Override
    @Transactional
    public void setStatus(Integer accoundId,Integer loadbalancerId,LoadBalancerStatus status) throws EntityNotFoundException{
        loadBalancerRepository.setStatus(accoundId, loadbalancerId, status);
    }

    @Override
    @Transactional
    public boolean testAndSetStatusPending(Integer accountId,Integer loadbalancerId) throws EntityNotFoundException, UnprocessableEntityException {
        return loadBalancerRepository.testAndSetStatus(accountId, loadbalancerId, LoadBalancerStatus.PENDING_UPDATE, false);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public LoadBalancer prepareForUpdate(LoadBalancer loadBalancer) throws Exception {
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

                if (loadBalancer.getProtocol().equals(HTTP)) {
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

        if (loadBalancer.isConnectionLogging() != null && !loadBalancer.isConnectionLogging().equals(dbLoadBalancer.isConnectionLogging())) {
            if (loadBalancer.isConnectionLogging()) {
                if (loadBalancer.getProtocol() != LoadBalancerProtocol.HTTP) {
                    LOG.error("Protocol must be HTTP for connection logging.");
                    throw new UnprocessableEntityException(String.format("Protocol must be HTTP for connection logging."));
                }
                LOG.debug("Enabling connection logging on the loadbalancer...");
            } else {
                LOG.debug("Disabling connection logging on the loadbalancer...");
            }
            dbLoadBalancer.setConnectionLogging(loadBalancer.isConnectionLogging());
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
    public LoadBalancer get(Integer id, Integer accountId) throws EntityNotFoundException {
        return loadBalancerRepository.getByIdAndAccountId(id, accountId);
    }

    @Override
    public List<org.openstack.atlas.service.domain.pojos.AccountLoadBalancer> getAccountLoadBalancers(Integer accountId) {
       return loadBalancerRepository.getAccountLoadBalancers(accountId);
    }

    @Override
    @Transactional
     public Suspension createSuspension(LoadBalancer loadBalancer, Suspension suspension) {
        return loadBalancerRepository.createSuspension(loadBalancer, suspension);
    }

    @Override
    @Transactional
    public void removeSuspension(int loadbalancerId) {
        loadBalancerRepository.removeSuspension(loadbalancerId);
    }

    @Override
    public LoadBalancer get(Integer id) throws EntityNotFoundException {
        return loadBalancerRepository.getById(id);
    }

    @Override
    @Transactional
    public LoadBalancer update(LoadBalancer lb) throws Exception {
        return loadBalancerRepository.update(lb);
    }

    @Override
    public AccountBilling getAccountBilling(Integer accountId, Calendar startTime, Calendar endTime) throws EntityNotFoundException {
        return loadBalancerRepository.getAccountBilling(accountId, startTime, endTime);
    }

    @Override
    public List<LoadBalancer> getLoadbalancersGeneric(Integer accountId,
                                                      String status, LbQueryStatus qs, Calendar changedCal,
                                                      Integer offset, Integer limit, Integer marker) throws BadRequestException {
        return loadBalancerRepository.getLoadbalancersGeneric(accountId, status, qs, changedCal, offset, limit, marker);
    }

    @Override
    @Transactional
    public void updateLoadBalancers(List<LoadBalancer> lbs) throws Exception {
        LOG.debug("Updating load balancers in database...");
        for (LoadBalancer lb : lbs) {
            LoadBalancer dbLb = get(lb.getId());
            if (lb.getHost() != null) dbLb.setHost(lb.getHost());
            dbLb.setStatus(LoadBalancerStatus.ACTIVE);
            update(dbLb);
        }
        LOG.debug("Successfully updated load balancers in database...");
    }

    @Override
    @Transactional
    public void setLoadBalancerAttrs(LoadBalancer lb) throws EntityNotFoundException {
        loadBalancerRepository.setLoadBalancerAttrs(lb);
    }

    @Override
    @Transactional
    public LoadBalancer prepareMgmtLoadBalancerDeletion(LoadBalancer loadBalancer, LoadBalancerStatus statusToCheck) throws EntityNotFoundException, UnprocessableEntityException {
       LOG.debug("Entering " + getClass());
        LoadBalancer dbLb = null;

        LOG.debug(String.format("%s del msgLB[%d]\n", loadBalancer.getId(), loadBalancer.getId()));

        dbLb = loadBalancerRepository.getById(loadBalancer.getId());

        //this operation only allows for loadbalancers to be deleted that are in ERROR status SITESLB-795
        if (!(dbLb.getStatus().equals(statusToCheck))) {
            String msg = String.format("%s msgLB[%d] dbLb[%d] status is %s and cannot be deleted. ", loadBalancer.getId(), loadBalancer.getId(), dbLb.getId(), dbLb.getStatus().toString());
            LOG.warn(msg);
            throw new UnprocessableEntityException(msg);
        }

        //this use case requires a loadbalancer in ERROR or SUSPENDED status to be deleted from LB Device and set to deleted in DB
        LOG.debug(String.format("Updating dbLB[%d] status to pending_delete", dbLb.getId()));
        dbLb.setStatus(LoadBalancerStatus.PENDING_DELETE);

        dbLb = loadBalancerRepository.update(dbLb);
        dbLb.setUserName(loadBalancer.getUserName());
        LOG.debug("Leaving " + getClass());
        return dbLb;
    }

    @Override
    public List<LoadBalancer> getLoadBalancersForAudit(String status, Calendar changedSince) throws Exception {
        LoadBalancerStatus error = null;
        LoadBalancerStatus build = null;
        LoadBalancerStatus pending_update = null;
        LoadBalancerStatus pending_delete = null;
        String[] statues = status.split("\\,");
        int statuesLength = statues.length;
        //map the values
        for (String stat : statues) {
            if (stat.equals("error")) error = LoadBalancerStatus.ERROR;
            if (stat.equals("build")) build = LoadBalancerStatus.BUILD;
            if (stat.equals("pending_update")) pending_update = LoadBalancerStatus.PENDING_UPDATE;
            if (stat.equals("pending_delete")) pending_delete = LoadBalancerStatus.PENDING_DELETE;
        }
        return loadBalancerRepository.getLoadBalancersStatusAndDate(error, build, pending_update, pending_delete, changedSince);
    }

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, ImmutableEntityException.class, UnprocessableEntityException.class})
    public void prepareForDelete(Integer accountId, List<Integer> loadBalancerIds) throws BadRequestException {
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

    @Override
    @Transactional
    public void prepareForDelete(LoadBalancer lb) throws Exception {
        List<Integer> loadBalancerIds = new ArrayList<Integer>();
        loadBalancerIds.add(lb.getId());
        prepareForDelete(lb.getAccountId(), loadBalancerIds);
    }


    @Override
    @Transactional
    public void pseudoDelete(LoadBalancer lb) throws Exception {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(lb.getId(), lb.getAccountId());
        dbLoadBalancer.setStatus(DELETED);
        dbLoadBalancer = loadBalancerRepository.update(dbLoadBalancer);

        virtualIpService.removeAllVipsFromLoadBalancer(dbLoadBalancer);
    }


    @Override
    public Boolean isLoadBalancerLimitReached(Integer accountId) {
        Boolean limitReached = false;

        try {
            LOG.debug(String.format("Obtaining load balancer limit for account '%d' from database...", accountId));
            Integer limit = accountLimitService.getLimit(accountId, AccountLimitType.LOADBALANCER_LIMIT);
            final Integer numNonDeletedLoadBalancers = loadBalancerRepository.getNumNonDeletedLoadBalancersForAccount(accountId);
            limitReached = (numNonDeletedLoadBalancers >= limit);
        } catch (EntityNotFoundException e) {
            LOG.error(String.format("No loadbalancer limit found. "
                    + "Customer with account '%d' could potentially be creating too many loadbalancers! "
                    + "Allowing operation to continue...", accountId), e);
            notificationService.saveAlert(accountId, null, e, AlertType.DATABASE_FAILURE.name(), "No loadbalancer limit found");
        }

        return limitReached;
    }

    @Override
    public Integer getLoadBalancerLimit(Integer accountId) throws EntityNotFoundException {
        return accountLimitService.getLimit(accountId, AccountLimitType.LOADBALANCER_LIMIT);
    }

    @Override
    @Transactional
    public void setStatus(LoadBalancer lb, LoadBalancerStatus status) {
        try {
            loadBalancerRepository.setStatus(lb, status);
        } catch (EntityNotFoundException e) {
            LOG.warn(String.format("Cannot set status for loadbalancer '%d' as it does not exist.", lb.getId()));
        }
    }

    @Override
    public void addDefaultValues(LoadBalancer loadBalancer) {
        loadBalancer.setStatus(BUILD);
        NodesHelper.setNodesToStatus(loadBalancer, NodeStatus.ONLINE);
        if (loadBalancer.getAlgorithm() == null) {
            loadBalancer.setAlgorithm(LoadBalancerAlgorithm.RANDOM);
        }
        if (loadBalancer.isConnectionLogging() == null) {
            loadBalancer.setConnectionLogging(false);
        }

        if (loadBalancer.getProtocol() == null || loadBalancer.getPort() == null) {
            LoadBalancerProtocolObject defaultProtocol = loadBalancerRepository.getDefaultProtocol();
            if (loadBalancer.getProtocol() == null) {
                loadBalancer.setProtocol(defaultProtocol.getName());
            }
            if (loadBalancer.getPort() == null) {
                loadBalancer.setPort(defaultProtocol.getPort());
            }
        }

        if (loadBalancer.getSessionPersistence() == null) {
            loadBalancer.setSessionPersistence(SessionPersistence.NONE);
        }

        for (Node node : loadBalancer.getNodes()) {
            if (node.getWeight() == null) {
                node.setWeight(Constants.DEFAULT_NODE_WEIGHT);
            }
        }
    }

    private void verifySessionPersistence(LoadBalancer queueLb) throws BadRequestException {
        if (queueLb.getSessionPersistence() != SessionPersistence.NONE) {
            LOG.info("Session Persistence detected. Verifying that the protocol is HTTP...");
            if (queueLb.getProtocol() != LoadBalancerProtocol.HTTP) {
                throw new BadRequestException("Protocol must be HTTP for session persistence.");
            }
        }
    }

    private void verifyProtocolAndHealthMonitorType(LoadBalancer queueLb) throws ProtocolHealthMonitorMismatchException {
        if (queueLb.getHealthMonitor() != null) {
            LOG.info("Health Monitor detected. Verifying that the load balancer's protocol matches the monitor type.");
            if (queueLb.getHealthMonitor().getType() != null) {
                if (queueLb.getHealthMonitor().getType().name().equals(HealthMonitorType.HTTP.name())) {
                    if (!(queueLb.getProtocol().equals(LoadBalancerProtocol.HTTP))) {
                        throw new ProtocolHealthMonitorMismatchException("Protocol must be HTTP for an HTTP health monitor.");
                    }
                } else if (queueLb.getHealthMonitor().getType().name().equals(HealthMonitorType.HTTPS.name())) {
                    if (!(queueLb.getProtocol().equals(LoadBalancerProtocol.HTTPS))) {
                        throw new ProtocolHealthMonitorMismatchException("Protocol must be HTTPS for an HTTPS health monitor.");
                    }
                }
            }
        }
    }


    private void verifyTCPProtocolandPort(LoadBalancer queueLb) throws TCPProtocolUnknownPortException {
        if (queueLb.getProtocol() != null && (queueLb.getProtocol().equals(LoadBalancerProtocol.TCP))) {
            LOG.info("TCP Protocol detected. Port must exists");
            if (queueLb.getPort() == null) {
                  throw new TCPProtocolUnknownPortException("Must Provide port for TCP Protocol.");
            }
        }
    }

    private void setHostForNewLoadBalancer(LoadBalancer loadBalancer) throws EntityNotFoundException, UnprocessableEntityException {
        boolean isHost = false;
        LoadBalancer gLb = new LoadBalancer();

        //Check for and grab host if sharing ipv4
        for (LoadBalancerJoinVip loadBalancerJoinVip : loadBalancer.getLoadBalancerJoinVipSet()) {
            if (loadBalancerJoinVip.getVirtualIp().getId() != null) {
                List<LoadBalancer> lbs = virtualIpRepository.getLoadBalancersByVipId(loadBalancerJoinVip.getVirtualIp().getId());
                for (LoadBalancer lb : lbs) {
                    String hostName = lb.getHost().getName();
                    if (lb.getHost().getName().equals(hostName)) {
                        gLb = lb;
                        isHost = true;
                    } else {
                        throw new UnprocessableEntityException("There was a conflict between the hosts while trying to share a virtual IP.");
                    }
                }
            }
        }

        //Check for and grab host if sharing ipv6
        for (LoadBalancerJoinVip6 loadBalancerJoinVip6 : loadBalancer.getLoadBalancerJoinVip6Set()) {
            if (loadBalancerJoinVip6.getVirtualIp().getId() != null) {
                List<LoadBalancer> lbs = virtualIpv6Repository.getLoadBalancersByVipId(loadBalancerJoinVip6.getVirtualIp().getId());
                for (LoadBalancer lb : lbs) {
                    String hostName = lb.getHost().getName();
                    if (lb.getHost().getName().equals(hostName)) {
                        gLb = lb;
                        isHost = true;
                    } else {
                        throw new UnprocessableEntityException("There was a conflict between the hosts while trying to share a virtual IP.");
                    }
                }
            }
        }

        if (isHost) {
            loadBalancer.setHost(gLb.getHost());
        } else {
            loadBalancer.setHost(hostService.getDefaultActiveHost());
        }
    }

    private void setVipConfigForLoadBalancer(LoadBalancer lbFromApi) throws OutOfVipsException, AccountMismatchException, UniqueLbPortViolationException, EntityNotFoundException, BadRequestException, ImmutableEntityException, UnprocessableEntityException {

        if (!lbFromApi.getLoadBalancerJoinVipSet().isEmpty()) {
            Set<LoadBalancerJoinVip> newVipConfig = new HashSet<LoadBalancerJoinVip>();
            List<VirtualIp> vipsOnAccount = virtualIpRepository.getVipsByAccountId(lbFromApi.getAccountId());
            for (LoadBalancerJoinVip loadBalancerJoinVip : lbFromApi.getLoadBalancerJoinVipSet()) {
                if (loadBalancerJoinVip.getVirtualIp().getId() == null) {
                    // Add a new vip to set
                    VirtualIp newVip = virtualIpService.allocateIpv4VirtualIp(loadBalancerJoinVip.getVirtualIp(), lbFromApi.getHost().getCluster());
                    LoadBalancerJoinVip newJoinRecord = new LoadBalancerJoinVip();
                    newJoinRecord.setVirtualIp(newVip);
                    newVipConfig.add(newJoinRecord);
                } else {
                    // Add shared vip to set
                    newVipConfig.addAll(getSharedIpv4Vips(loadBalancerJoinVip.getVirtualIp(), vipsOnAccount, lbFromApi.getPort()));
                }
            }
            lbFromApi.setLoadBalancerJoinVipSet(newVipConfig);
        }

        if (!lbFromApi.getLoadBalancerJoinVip6Set().isEmpty()) {
            Set<LoadBalancerJoinVip6> newVip6Config = new HashSet<LoadBalancerJoinVip6>();
            List<VirtualIpv6> vips6OnAccount = virtualIpv6Repository.getVips6ByAccountId(lbFromApi.getAccountId());
            Set<LoadBalancerJoinVip6> loadBalancerJoinVip6SetConfig = lbFromApi.getLoadBalancerJoinVip6Set();
            lbFromApi.setLoadBalancerJoinVip6Set(null);
            for (LoadBalancerJoinVip6 loadBalancerJoinVip6 : loadBalancerJoinVip6SetConfig) {
                if (loadBalancerJoinVip6.getVirtualIp().getId() == null) {
                    VirtualIpv6 ipv6 = virtualIpService.allocateIpv6VirtualIp(lbFromApi);
                    LoadBalancerJoinVip6 jbjv6 = new LoadBalancerJoinVip6();
                    jbjv6.setVirtualIp(ipv6);
                    newVip6Config.add(jbjv6);
                } else {
                    //share ipv6 vip here..
                    newVip6Config.addAll(getSharedIpv6Vips(loadBalancerJoinVip6.getVirtualIp(), vips6OnAccount, lbFromApi.getPort()));
                }
                lbFromApi.setLoadBalancerJoinVip6Set(newVip6Config);
            }
        }
    }

    private Set<LoadBalancerJoinVip> getSharedIpv4Vips(VirtualIp vipConfig, List<VirtualIp> vipsOnAccount, Integer lbPort) throws AccountMismatchException, UniqueLbPortViolationException {
        Set<LoadBalancerJoinVip> sharedVips = new HashSet<LoadBalancerJoinVip>();
        boolean belongsToProperAccount = false;

        // Verify this is a valid virtual ip to share
        for (VirtualIp vipOnAccount : vipsOnAccount) {
            if (vipOnAccount.getId().equals(vipConfig.getId())) {
                if(virtualIpService.isIpv4VipPortCombinationInUse(vipOnAccount, lbPort)) {
                    throw new UniqueLbPortViolationException("Another load balancer is currently using the requested port with the shared virtual ip.");
                }
                belongsToProperAccount = true;
                LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip();
                loadBalancerJoinVip.setVirtualIp(vipOnAccount);
                sharedVips.add(loadBalancerJoinVip);
            }
        }

        if (!belongsToProperAccount) {
            throw new AccountMismatchException("Invalid requesting account for the shared virtual ip.");
        }
        return sharedVips;
    }

    private Set<LoadBalancerJoinVip6> getSharedIpv6Vips(VirtualIpv6 vipConfig, List<VirtualIpv6> vipsOnAccount, Integer lbPort) throws AccountMismatchException, UniqueLbPortViolationException {
        Set<LoadBalancerJoinVip6> sharedVips = new HashSet<LoadBalancerJoinVip6>();
        boolean belongsToProperAccount = false;

        // Verify this is a valid virtual ip to share
        for (VirtualIpv6 vipOnAccount : vipsOnAccount) {
            if (vipOnAccount.getId().equals(vipConfig.getId())) {
                if(virtualIpService.isIpv6VipPortCombinationInUse(vipOnAccount, lbPort)) {
                    throw new UniqueLbPortViolationException("Another load balancer is currently using the requested port with the shared virtual ip.");
                }
                belongsToProperAccount = true;
                LoadBalancerJoinVip6 loadBalancerJoinVip6 = new LoadBalancerJoinVip6();
                loadBalancerJoinVip6.setVirtualIp(vipOnAccount);
                sharedVips.add(loadBalancerJoinVip6);
            }
        }

        if (!belongsToProperAccount) {
            throw new AccountMismatchException("Invalid requesting account for the shared virtual ip.");
        }
        return sharedVips;
    }

    @Transactional
    private void joinIpv6OnLoadBalancer(LoadBalancer lb) {
        Set<LoadBalancerJoinVip6> loadBalancerJoinVip6SetConfig = lb.getLoadBalancerJoinVip6Set();
        lb.setLoadBalancerJoinVip6Set(null);
        Set<LoadBalancerJoinVip6> newLbVip6Setconfig = new HashSet<LoadBalancerJoinVip6>();
        lb.setLoadBalancerJoinVip6Set(newLbVip6Setconfig);
        for (LoadBalancerJoinVip6 jv6: loadBalancerJoinVip6SetConfig) {
        LoadBalancerJoinVip6 jv = new LoadBalancerJoinVip6(lb.getPort(), lb, jv6.getVirtualIp());
        virtualIpRepository.persist(jv);
        }
    }

    @Override
    public SessionPersistence getSessionPersistenceByAccountIdLoadBalancerId(Integer accountId,
                                                                             Integer loadbalancerId) throws EntityNotFoundException, DeletedStatusException, BadRequestException {
        SessionPersistence sp;
        sp = loadBalancerRepository.getSessionPersistenceByAccountIdLoadBalancerId(accountId, loadbalancerId);
        return sp;
    }

    @Override
    @Transactional
    public List<LoadBalancer> reassignLoadBalancerHost(List<LoadBalancer> lbs) throws Exception{
        List<LoadBalancer> invalidLbs = new ArrayList<LoadBalancer>();
        List<LoadBalancer> validLbs = new ArrayList<LoadBalancer>();
        LoadBalancer dbLb;

        for (LoadBalancer lb : lbs) {
            dbLb = loadBalancerRepository.getById(lb.getId());
            if (dbLb.isSticky()) {
                invalidLbs.add(dbLb);
            } else {
                setStatus(dbLb, LoadBalancerStatus.PENDING_UPDATE);
                processSpecifiedOrDefaultHost(lb);
                validLbs.add(lb);
            }
        }

        //inform user of all invalid Load balancers...
        String[] array = new String[invalidLbs.size()];
        for (int i = 0; i < invalidLbs.size(); i++) {
            array[i] = invalidLbs.get(i).getId().toString();
        }

        if (!invalidLbs.isEmpty()) {
            throw new BadRequestException("Found sticky LoadBalancers (" + StringUtilities.buildDelemtedListFromStringArray(array, ",") + ") please remove and retry the request");
        }
        return validLbs;
    }

    private void processSpecifiedOrDefaultHost(LoadBalancer lb) throws EntityNotFoundException, BadRequestException {
        Integer hostId = null;
        Host specifiedHost;

        if (lb.getHost() != null ) hostId = lb.getHost().getId();
        if (!lb.isSticky()) {
            if (hostId != null) {
                specifiedHost = hostService.getById(hostId);
                if (!(specifiedHost.getHostStatus().equals(HostStatus.ACTIVE) || specifiedHost.getHostStatus().equals(HostStatus.ACTIVE_TARGET))) {
                    setStatus(lb, LoadBalancerStatus.ACTIVE);
                    throw new BadRequestException("Load balancers cannot move to a host(" + specifiedHost.getId() + ") that is not in ACTIVE or ACTIVE_TARGET status.");
                }
                lb.setHost(specifiedHost);
            } else {
                lb.setHost(hostService.getDefaultActiveHost());
            }
        }
    }
}

