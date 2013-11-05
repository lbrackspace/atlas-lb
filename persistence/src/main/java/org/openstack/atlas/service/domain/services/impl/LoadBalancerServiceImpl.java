package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.docs.loadbalancers.api.v1.ProtocolPortBindings;
import org.openstack.atlas.service.domain.cache.AtlasCache;
import org.openstack.atlas.service.domain.deadlock.DeadLockRetry;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.pojos.AccountBilling;
import org.openstack.atlas.service.domain.pojos.ExtendedAccountLoadBalancer;
import org.openstack.atlas.service.domain.pojos.LbQueryStatus;
import org.openstack.atlas.service.domain.services.*;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.openstack.atlas.service.domain.services.helpers.NodesHelper;
import org.openstack.atlas.service.domain.services.helpers.NodesPrioritiesContainer;
import org.openstack.atlas.service.domain.services.helpers.StringHelper;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;
import org.openstack.atlas.service.domain.util.CacheKeyGen;
import org.openstack.atlas.service.domain.util.Constants;
import org.openstack.atlas.service.domain.util.StringUtilities;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.openstack.atlas.util.ip.exception.IpTypeMissMatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.*;
import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.BUILD;
import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.DELETED;
import static org.openstack.atlas.service.domain.entities.SessionPersistence.*;

@Service
public class LoadBalancerServiceImpl extends BaseService implements LoadBalancerService {

    private final Log LOG = LogFactory.getLog(LoadBalancerServiceImpl.class);

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private AccountLimitService accountLimitService;
    @Autowired
    private VirtualIpService virtualIpService;
    @Autowired
    private HostService hostService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private LoadBalancerStatusHistoryService loadBalancerStatusHistoryService;
    @Autowired
    private AtlasCache atlasCache;

    @Override
    @Transactional
    public String getErrorPage(Integer lid, Integer aid) throws EntityNotFoundException {
        return loadBalancerRepository.getErrorPage(lid, aid);
    }

    @Override
    @Transactional
    public String getDefaultErrorPage() throws EntityNotFoundException {
        Defaults defaultPage = loadBalancerRepository.getDefaultErrorPage();
        if (defaultPage == null) {
            throw new EntityNotFoundException("The default error page could not be located.");
        }
        return defaultPage.getValue();
    }

    @Override
    @DeadLockRetry
    @Transactional
    public LoadBalancer create(LoadBalancer lb) throws Exception {
        if (isLoadBalancerLimitReached(lb.getAccountId())) {
            LOG.error("Load balancer limit reached. Sending error response to client...");
            throw new LimitReachedException(String.format("Load balancer limit reached. "
                    + "Limit is set to '%d'. Contact support if you would like to increase your limit.",
                    getLoadBalancerLimit(lb.getAccountId())));
        }

        // Drop Health Monitor code here for secNodes

        // If user wants secondary nodes they must have some kind of healthmonitoring
        NodesPrioritiesContainer npc = new NodesPrioritiesContainer(lb.getNodes());
        if (lb.getHealthMonitor() == null && npc.hasSecondary()) {
            throw new BadRequestException(Constants.NoMonitorForSecNodes);
        }

        // HTTPS Redirect is only valid for HTTPS LBs or LBs with SSL Termination
        if (lb.isHttpsRedirect() != null && lb.isHttpsRedirect()) {
            if (!lb.getProtocol().equals(LoadBalancerProtocol.HTTPS)) {
                throw new BadRequestException("HTTPS Redirect is only valid for load balancers using the HTTPS protocol, " +
                        "or for load balancers with a 'Secure Only' SSL Termination.");
            } else if (lb.getPort() != null && lb.getPort() != 443) { //We just redirect to https://original.url.com which goes to 443
                throw new BadRequestException("HTTPS Redirect can only be enabled for HTTPS load balancers using port 443.");
            }
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

        if (nodeService.detectDuplicateNodes(new LoadBalancer(), lb)) {
            throw new BadRequestException("Duplicate nodes detected. Please provide a list of unique node addresses.");
        }

        if (isNodeLimitReached(lb)) {
            throw new LimitReachedException(String.format("Node limit for this load balancer exceeded."));
        }

        try {
            //check for TCP protocol and port before adding default, since TCP protocol has no default
            verifyTCPUDPProtocolandPort(lb);
            addDefaultValues(lb);
            //V1-B-17728 allowing ip SP for non-http protocols
            verifySessionPersistence(lb);
            verifyProtocolAndHealthMonitorType(lb);
            verifyHalfCloseSupport(lb);
            verifyContentCaching(lb);
            setHostForNewLoadBalancer(lb);
            setVipConfigForLoadBalancer(lb);
        } catch (UniqueLbPortViolationException e) {
            LOG.warn("The port of the new LB is the same as the LB to which you wish to share a virtual ip.");
            throw e;
        } catch (AccountMismatchException e) {
            LOG.warn("The accounts do not match for the requested shared virtual ip.");
            throw e;
        } catch (BadRequestException e) {
            LOG.debug(e.getMessage());
            throw e;
        } catch (ProtocolHealthMonitorMismatchException e) {
            LOG.warn("Protocol type of HTTP/HTTPS must match Health Monitor Type of HTTP/HTTPS.");
            throw e;
        } catch (TCPProtocolUnknownPortException e) {
            LOG.warn("Port must be supplied for TCP Protocol.");
            throw e;
        } catch (UnprocessableEntityException e) {
            LOG.warn("There is an error regarding the virtual IP hosts, with a shared virtual IP the LoadBalancers must reside within the same cluster.");
            throw e;
        } catch (OutOfVipsException e) {
            LOG.warn("Out of virtual ips! Sending error response to client...");
            String errorMessage = e.getMessage();
            notificationService.saveAlert(lb.getAccountId(), lb.getId(), e, AlertType.API_FAILURE.name(), errorMessage);
            throw e;
        } catch (IllegalArgumentException e) {
            LOG.warn("Virtual Ip could not be processed....");
            String errorMessage = e.getMessage();
            notificationService.saveAlert(lb.getAccountId(), lb.getId(), e, AlertType.API_FAILURE.name(), errorMessage);
            throw e;
        }

        LoadBalancer dbLoadBalancer = loadBalancerRepository.create(lb);
        dbLoadBalancer.setUserName(lb.getUserName());
        joinIpv6OnLoadBalancer(dbLoadBalancer);

        // Add atom entry
//        String atomTitle = "Load Balancer in build status";
//        String atomSummary = "Load balancer in build status";
//        notificationService.saveLoadBalancerEvent(lb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), atomTitle, atomSummary, BUILD_LOADBALANCER, CREATE, INFO);

        //Save history record
        loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.BUILD);

        return dbLoadBalancer;
    }

    @Override
    @Transactional
    public void setStatus(Integer accoundId, Integer loadbalancerId, LoadBalancerStatus status) throws EntityNotFoundException {
        loadBalancerRepository.setStatus(accoundId, loadbalancerId, status);
    }

    @Override
    @Transactional
    public boolean testAndSetStatusPending(Integer accountId, Integer loadbalancerId) throws EntityNotFoundException, UnprocessableEntityException {
        return loadBalancerRepository.testAndSetStatus(accountId, loadbalancerId, LoadBalancerStatus.PENDING_UPDATE, false);
    }

    @Override
    @Transactional
    public boolean testAndSetStatus(Integer accountId, Integer loadbalancerId, LoadBalancerStatus loadBalancerStatus) throws EntityNotFoundException, UnprocessableEntityException {
        boolean isStatusSet;
        isStatusSet = loadBalancerRepository.testAndSetStatus(accountId, loadbalancerId, loadBalancerStatus, false);
        if (isStatusSet) {
            loadBalancerStatusHistoryService.save(accountId, loadbalancerId, loadBalancerStatus);
            return isStatusSet;
        }

        return isStatusSet;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public LoadBalancer prepareForUpdate(LoadBalancer loadBalancer) throws Exception {
        LoadBalancer dbLoadBalancer;
        boolean portHMTypecheck = true;

        dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(loadBalancer.getId(), loadBalancer.getAccountId());

        if (dbLoadBalancer.hasSsl()) {
            LOG.debug("Verifying protocol, cannot update protocol while using ssl termination...");
            if (loadBalancer.getProtocol() != null && loadBalancer.getProtocol() != dbLoadBalancer.getProtocol()) {
                throw new BadRequestException("Cannot update protocol on a load balancer with ssl termination.");
            }
//            SslTerminationHelper.isProtocolSecure(loadBalancer);
        }

        LOG.info("Performing SSL verifications.");
        if (dbLoadBalancer.hasSsl()) {
            LOG.info("Verifying unique ports against SSL termination securePort.");
            SslTermination ssl = dbLoadBalancer.getSslTermination();
            if (loadBalancer.getPort() != null && loadBalancer.getPort() == ssl.getSecurePort()) {
                LOG.error("Cannot update load balancer port as it is currently in use by ssl termination.");
                throw new BadRequestException(String.format("Port currently assigned to SSL termination for this load balancer. Please try another port."));
            } else if (loadBalancer.getPort() != null) {
                LOG.info(String.format("Load balancer port:%d  and SSL Termination port:%d are unique, continue...", loadBalancer.getPort(), ssl.getSecurePort()));
            } else {
                LOG.info(String.format("Load balancer port:%d  and SSL Termination port:%d are unique, continue...", dbLoadBalancer.getPort(), ssl.getSecurePort()));
            }

            //Validation for HTTPS redirect
            LOG.info("Verifying HTTPS redirect status.");
            if ((loadBalancer.isHttpsRedirect() != null && loadBalancer.isHttpsRedirect()) ||
                (loadBalancer.isHttpsRedirect() == null && dbLoadBalancer.isHttpsRedirect() != null && dbLoadBalancer.isHttpsRedirect())) {
                if (!ssl.isSecureTrafficOnly()) {
                    LOG.error("Cannot use HTTPS Redirect on a load balancer with a mixed-mode SSL termination.");
                    throw new BadRequestException("HTTPS Redirect is only valid for load balancers using the HTTPS protocol, " +
                            "or for load balancers with a 'Secure Only' SSL Termination.");
                } else if (ssl.getSecurePort() != 443) { //This would be a ridiculous configuration
                    LOG.error("HTTPS Redirect can only be enabled for load balancers with SSL Termination using secure port 443.");
                    throw new BadRequestException("HTTPS Redirect can only be enabled for load balancers with SSL Termination using secure port 443.");
                } else if (loadBalancer.getPort() != null && loadBalancer.getPort() != 80) { //dbLoadbalancer doesn't matter, we just can't let them change the port manually after enabling HTTPS-R
                    LOG.error("Cannot change port on load balancers with HTTPS Redirect enabled.");
                    throw new BadRequestException("Cannot change port on load balancers with HTTPS Redirect enabled.");
                }
            }
        } else {
            if ((loadBalancer.isHttpsRedirect() != null && loadBalancer.isHttpsRedirect()) ||
                    (loadBalancer.isHttpsRedirect() == null && dbLoadBalancer.isHttpsRedirect() != null && dbLoadBalancer.isHttpsRedirect())) {
                if ((loadBalancer.getProtocol() != null && !loadBalancer.getProtocol().equals(LoadBalancerProtocol.HTTPS))
                  || loadBalancer.getProtocol() == null && !dbLoadBalancer.getProtocol().equals(LoadBalancerProtocol.HTTPS)) {
                    LOG.error("HTTPS Redirect can only be enabled for HTTPS or SSL load balancers.");
                    throw new BadRequestException("HTTPS Redirect is only valid for load balancers using the HTTPS protocol, " +
                            "or for load balancers with a 'Secure Only' SSL Termination.");
                } else if ((loadBalancer.getPort() != null && loadBalancer.getPort() != 443)
                        || (loadBalancer.getPort() == null && dbLoadBalancer.getPort() != 443)) {
                //We just redirect to https://original.url.com which goes to 443
                    LOG.error("HTTPS Redirect can only be enabled for HTTPS load balancers using port 443.");
                    throw new BadRequestException("HTTPS Redirect can only be enabled for HTTPS load balancers using port 443.");
                }
            }
        }

        LOG.debug("Updating the lb status to pending_update");
        if (!testAndSetStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE)) {
            String message = StringHelper.immutableLoadBalancer(dbLoadBalancer);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        }

        if (loadBalancer.getPort() != null && !loadBalancer.getPort().equals(dbLoadBalancer.getPort())) {
            LOG.debug("Updating loadbalancer port to " + loadBalancer.getPort());
            if (loadBalancerRepository.canUpdateToNewPort(loadBalancer.getPort(), dbLoadBalancer)) {
                loadBalancerRepository.updatePortInJoinTable(loadBalancer);
                dbLoadBalancer.setPort(loadBalancer.getPort());
            } else {
                LOG.error("Cannot update load balancer port as it is currently in use by another virtual ip and could be in conflict with the load balancer protocol.");
                throw new BadRequestException(String.format("Port currently assigned to one of the virtual ips. Please verify protocol/port combinations."));
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

        if (loadBalancer.getTimeout() != null && !loadBalancer.getTimeout().equals(dbLoadBalancer.getTimeout())) {
            LOG.debug("Updating loadbalancer timeout to " + loadBalancer.getTimeout());
            dbLoadBalancer.setTimeout(loadBalancer.getTimeout());
        }

        if (loadBalancer.getProtocol() != null && !loadBalancer.getProtocol().equals(dbLoadBalancer.getProtocol())) {
            verifyTCPUDPProtocolandPort(loadBalancer, dbLoadBalancer);

            if (loadBalancer.isHalfClosed() != null) {
                verifyHalfCloseSupport(loadBalancer);
            } else {
                verifyHalfCloseSupport(loadBalancer, dbLoadBalancer.isHalfClosed());
            }

            boolean isValidProto = true;
            if (checkLBProtocol(loadBalancer)) {
                //Move
                if (loadBalancer.getPort() != null) {
                    dbLoadBalancer.setPort(loadBalancer.getPort());
                } else {
                    dbLoadBalancer.setPort(ProtocolPortBindings.getPortByKey(loadBalancer.getProtocol().toString()));
                }

                for (LoadBalancerJoinVip vip : dbLoadBalancer.getLoadBalancerJoinVipSet()) {
                    List<LoadBalancer> sharedLbs = virtualIpRepository.getLoadBalancersByVipId(vip.getVirtualIp().getId());
                    List<LoadBalancer> sharedLbsToCheck = new ArrayList<LoadBalancer>();

                    for (LoadBalancer lb : sharedLbs) {
                        if (!lb.getId().equals(loadBalancer.getId())) {
                            sharedLbsToCheck.add(lb);
                        }
                    }
                    isValidProto = verifySharedVipProtocols(sharedLbsToCheck, loadBalancer);
                }

                for (LoadBalancerJoinVip6 vip6 : dbLoadBalancer.getLoadBalancerJoinVip6Set()) {
                    List<LoadBalancer> sharedLbs = virtualIpv6Repository.getLoadBalancersByVipId(vip6.getVirtualIp().getId());
                    List<LoadBalancer> sharedLbsToCheck = new ArrayList<LoadBalancer>();

                    for (LoadBalancer lb : sharedLbs) {
                        if (!lb.getId().equals(loadBalancer.getId())) {
                            sharedLbsToCheck.add(lb);
                        }
                    }
                    isValidProto = verifySharedVipProtocols(sharedLbsToCheck, loadBalancer);
                }
            }

            if (!isValidProto) {
                throw new BadRequestException("Protocol is not valid. Please verify shared virtual ips and the ports being shared.");
            }


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
                    if ((dbLoadBalancer.getSessionPersistence() == SessionPersistence.HTTP_COOKIE)) {
                        LOG.debug("Updating loadbalancer protocol to " + loadBalancer.getProtocol());
                        dbLoadBalancer.setProtocol(loadBalancer.getProtocol());
                    } else {
                        LOG.debug("Updating loadbalancer protocol to " + SessionPersistence.NONE);
                        dbLoadBalancer.setSessionPersistence(SessionPersistence.NONE);
                        dbLoadBalancer.setProtocol(loadBalancer.getProtocol());
                    }

                } else if (!loadBalancer.getProtocol().equals(HTTP)) {
                    dbLoadBalancer.setContentCaching(false);
                    if ((dbLoadBalancer.getSessionPersistence() == SessionPersistence.SOURCE_IP)) {
                        LOG.debug("Updating loadbalancer protocol to " + loadBalancer.getProtocol());
                        dbLoadBalancer.setProtocol(loadBalancer.getProtocol());
                    } else {
                        LOG.debug("Updating loadbalancer protocol to " + SessionPersistence.NONE);
                        dbLoadBalancer.setSessionPersistence(SessionPersistence.NONE);
                        dbLoadBalancer.setProtocol(loadBalancer.getProtocol());
                    }
                }
            } else {
                LOG.error("Cannot update port as the loadbalancer has a incompatible Health Monitor type");
                throw new BadRequestException(String.format("Cannot update port as the loadbalancer has a incompatible Health Monitor type"));
            }
        }

        LOG.debug(String.format("Verifying connectionLogging and contentCaching... if enabled, they are valid only with HTTP protocol.."));
        verifyProtocolLoggingAndCaching(loadBalancer, dbLoadBalancer);

        //V1-B-27058 12-10-12
        LOG.debug("Update half close support in load balancer service impl");
        if (loadBalancer.isHalfClosed() != null) {
            verifyHalfCloseSupport(dbLoadBalancer, loadBalancer.isHalfClosed());
            dbLoadBalancer.setHalfClosed(loadBalancer.isHalfClosed());
        }

        LOG.debug("Updating loadbalancer httpsRedirect to " + loadBalancer.isHttpsRedirect());
        if (loadBalancer.isHttpsRedirect() != null) {
            dbLoadBalancer.setHttpsRedirect(loadBalancer.isHttpsRedirect());
        }

        dbLoadBalancer = loadBalancerRepository.update(dbLoadBalancer);
        dbLoadBalancer.setUserName(loadBalancer.getUserName());
        LOG.debug("Updated the loadbalancer in DB. Now sending response back.");

//        // Add atom entry
//        String atomTitle = "Load Balancer in pending update status";
//        String atomSummary = "Load balancer in pending update status";
//        notificationService.saveLoadBalancerEvent(loadBalancer.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), atomTitle, atomSummary, PENDING_UPDATE_LOADBALANCER, UPDATE, INFO);

        // TODO: Sending db loadbalancer causes everything to update. Tweek for performance
        LOG.debug("Leaving " + getClass());
        return dbLoadBalancer;
    }

    private void verifyProtocolLoggingAndCaching(LoadBalancer loadBalancer, LoadBalancer dbLoadBalancer) throws UnprocessableEntityException {
        String logErr = "Protocol must be HTTP for connection logging.";
        String ccErr = "Protocol must be HTTP for content caching.";
        String enable = " is Being enabled on the loadbalancer";
        String disable = " is Being disabled on the loadbalancer";

        if (loadBalancer.isConnectionLogging() != null && !loadBalancer.isConnectionLogging().equals(dbLoadBalancer.isConnectionLogging())) {
            if (loadBalancer.isConnectionLogging()) {
                if (loadBalancer.getProtocol() != LoadBalancerProtocol.HTTP) {
                    LOG.error(logErr);
                    throw new UnprocessableEntityException(logErr);
                }
                LOG.debug("ConnectionLogging" + enable);
            } else {
                LOG.debug("ConnectionLogging" + disable);
            }
            dbLoadBalancer.setConnectionLogging(loadBalancer.isConnectionLogging());
        }

        if (loadBalancer.isContentCaching() != null && !loadBalancer.isContentCaching().equals(dbLoadBalancer.isConnectionLogging())) {
            if (loadBalancer.isContentCaching()) {
                if (loadBalancer.getProtocol() != LoadBalancerProtocol.HTTP) {
                    LOG.error(ccErr);
                    throw new UnprocessableEntityException(ccErr);
                }
                LOG.debug("ContentCaching" + enable);
            } else {
                LOG.debug("ContentCaching" + disable);
            }
            dbLoadBalancer.setConnectionLogging(loadBalancer.isConnectionLogging());
        }


    }

    @Transactional
    public UserPages getUserPages(Integer id, Integer accountId) throws EntityNotFoundException {
        LoadBalancer dLb = loadBalancerRepository.getByIdAndAccountId(id, accountId);
        UserPages up = dLb.getUserPages();
        return up;
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
    public List<ExtendedAccountLoadBalancer> getExtendedAccountLoadBalancer(Integer accountId) {
        String key = CacheKeyGen.generateKeyName(accountId);
        ArrayList<ExtendedAccountLoadBalancer> extendedAccountLoadBalancers;
        boolean b = atlasCache.containsKey(key);
        extendedAccountLoadBalancers = (ArrayList<ExtendedAccountLoadBalancer>) atlasCache.get(key);
        if (extendedAccountLoadBalancers == null) {
            LOG.debug("Setting ExtendedLoadBalancers in cache for: " + " at " + Calendar.getInstance().getTime().toString());
            extendedAccountLoadBalancers = loadBalancerRepository.getExtendedAccountLoadBalancers(accountId);
            atlasCache.set(key, extendedAccountLoadBalancers);
        } else {
            LOG.debug("Retrieved ExtendedLoadBalancers from cache for: " + accountId + " at " + Calendar.getInstance().getTime().toString());
            return extendedAccountLoadBalancers;
        }

        return extendedAccountLoadBalancers;
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
            if (lb.getHost() != null) {
                dbLb.setHost(lb.getHost());
            }
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

        //this use case requires a loadbalancer in ERROR or SUSPENDED status to be deleted from Zeus and set to deleted in DB
        LOG.debug(String.format("Updating dbLB[%d] status to pending_delete", dbLb.getId()));
        dbLb.setStatus(LoadBalancerStatus.PENDING_DELETE);

        dbLb = loadBalancerRepository.update(dbLb);
        dbLb.setUserName(loadBalancer.getUserName());

        // Add atom entry
//        String atomTitle = "Load Balancer in pending delete status";
//        String atomSummary = "Load balancer in pending delete status";
//        notificationService.saveLoadBalancerEvent(loadBalancer.getUserName(), loadBalancer.getAccountId(), loadBalancer.getId(), atomTitle, atomSummary, PENDING_DELETE_LOADBALANCER, DELETE, INFO);

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
            if (stat.equals("error")) {
                error = LoadBalancerStatus.ERROR;
            }
            if (stat.equals("build")) {
                build = LoadBalancerStatus.BUILD;
            }
            if (stat.equals("pending_update")) {
                pending_update = LoadBalancerStatus.PENDING_UPDATE;
            }
            if (stat.equals("pending_delete")) {
                pending_delete = LoadBalancerStatus.PENDING_DELETE;
            }
        }
        return loadBalancerRepository.getLoadBalancersStatusAndDate(error, build, pending_update, pending_delete, changedSince);
    }

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, ImmutableEntityException.class, UnprocessableEntityException.class, BadRequestException.class})
    public List<LoadBalancer> prepareForDelete(Integer accountId, List<Integer> loadBalancerIds) throws BadRequestException {
        List<Integer> badLbIds = new ArrayList<Integer>();
        List<Integer> badLbStatusIds = new ArrayList<Integer>();

        List<LoadBalancer> loadBalancers = new ArrayList<LoadBalancer>();
        for (int lbIdToDelete : loadBalancerIds) {
            try {
                LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(lbIdToDelete, accountId);
                if (!loadBalancerRepository.testAndSetStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_DELETE, false)) {
                    LOG.warn(StringHelper.immutableLoadBalancer(dbLoadBalancer));
                    badLbStatusIds.add(lbIdToDelete);
                } else {
                    //Set status record
                    loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_DELETE);

//                    // Add atom entry
//                    String atomTitle = "Load Balancer in pending delete status";
//                    String atomSummary = "Load balancer in pending delete status";
//                    notificationService.saveLoadBalancerEvent(dbLoadBalancer.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), atomTitle, atomSummary, PENDING_DELETE_LOADBALANCER, DELETE, INFO);
                }
                loadBalancers.add(dbLoadBalancer);
            } catch (Exception e) {
                badLbIds.add(lbIdToDelete);
            }
        }
        if (!badLbIds.isEmpty()) {
            throw new BadRequestException(String.format("Must provide valid load balancers: %s  could not be found.", StringUtilities.DelimitString(badLbIds, ",")));
        }
        if (!badLbStatusIds.isEmpty()) {
            throw new BadRequestException(String.format("Must provide valid load balancers: %s  are immutable and could not be processed.", StringUtilities.DelimitString(badLbStatusIds, ",")));
        }

        return loadBalancers;
    }

    @Override
    @Transactional
    public void prepareForDelete(LoadBalancer lb) throws Exception {
        List<Integer> loadBalancerIds = new ArrayList<Integer>();
        loadBalancerIds.add(lb.getId());
        prepareForDelete(lb.getAccountId(), loadBalancerIds);
    }

    @Override
    @DeadLockRetry
    @Transactional
    public LoadBalancer pseudoDelete(LoadBalancer lb) throws Exception {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(lb.getId(), lb.getAccountId());
        //Remove error page
        loadBalancerRepository.removeErrorPage(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId());
        dbLoadBalancer.setStatus(DELETED);
        dbLoadBalancer = loadBalancerRepository.update(dbLoadBalancer);

        virtualIpService.removeAllVipsFromLoadBalancer(dbLoadBalancer);

        return dbLoadBalancer;
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

    public Boolean isNodeLimitReached(LoadBalancer loadBalancer) {
        try {
            LOG.debug(String.format("Obtaining node limit for acount '%d' from database...", loadBalancer.getAccountId()));
            Integer limit = accountLimitService.getLimit(loadBalancer.getAccountId(), AccountLimitType.NODE_LIMIT);
            if (loadBalancer.getNodes().size() > limit) {
                return true;
            }
        } catch (EntityNotFoundException e) {
            LOG.error(String.format("No node limit found. "
                    + "Customer with account '%d' could potentially be creating too many nodes! "
                    + "Allowing operation to continue...", loadBalancer.getAccountId()), e);
        }
        return false;
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
            LoadBalancer dbLb = loadBalancerRepository.getById(lb.getId());
            loadBalancerStatusHistoryService.save(dbLb.getAccountId(), dbLb.getId(), status);

        } catch (EntityNotFoundException e) {
            LOG.warn(String.format("Cannot set status for loadbalancer '%d' as it does not exist.", lb.getId()));
        }
    }

    @Override
    public void addDefaultValues(LoadBalancer loadBalancer) {
        final Integer TIMEOUT_DEFAULT = 30;

        loadBalancer.setStatus(BUILD);
        NodesHelper.setNodesToStatus(loadBalancer, NodeStatus.ONLINE);
        if (loadBalancer.getAlgorithm() == null) {
            loadBalancer.setAlgorithm(LoadBalancerAlgorithm.RANDOM);
        }
        if (loadBalancer.isConnectionLogging() == null) {
            loadBalancer.setConnectionLogging(false);
        }

        if ((loadBalancer.getProtocol() == null && loadBalancer.getPort() == null) || (loadBalancer.getProtocol() == null && loadBalancer.getPort() != null)) {
            LoadBalancerProtocolObject defaultProtocol = loadBalancerRepository.getDefaultProtocol();
            loadBalancer.setProtocol(defaultProtocol.getName());
            loadBalancer.setPort(defaultProtocol.getPort());
        } else if (loadBalancer.getProtocol() != null && loadBalancer.getPort() == null) {
            LoadBalancerProtocolObject protocol = loadBalancerRepository.getProtocol(loadBalancer.getProtocol());
            loadBalancer.setPort(protocol.getPort());
        }

        if (loadBalancer.getSessionPersistence() == null) {
            loadBalancer.setSessionPersistence(SessionPersistence.NONE);
        }

        for (Node node : loadBalancer.getNodes()) {
            if (node.getWeight() == null) {
                node.setWeight(Constants.DEFAULT_NODE_WEIGHT);
            }
        }

        if (loadBalancer.getTimeout() == null) {
            loadBalancer.setTimeout(TIMEOUT_DEFAULT);
        }

        if (loadBalancer.isHalfClosed() == null) {
            loadBalancer.setHalfClosed(false);
        }

        if (loadBalancer.isHttpsRedirect() == null) {
            loadBalancer.setHttpsRedirect(false);
        }
    }

    private void verifySessionPersistence(LoadBalancer queueLb) throws BadRequestException {
        //Dupelicated in sessionPersistenceServiceImpl ...
        SessionPersistence inpersist = queueLb.getSessionPersistence();
        LoadBalancerProtocol dbProtocol = queueLb.getProtocol();

        String httpErrMsg = "HTTP_COOKIE Session persistence is only valid with HTTP and HTTP pass-through(ssl-termination) protocols.";
        String sipErrMsg = "SOURCE_IP Session persistence is only valid with non HTTP protocols.";
        if (inpersist != NONE) {
            if (inpersist == HTTP_COOKIE
                    && (dbProtocol != HTTP)) {
                throw new BadRequestException(httpErrMsg);
            }

            if (inpersist == SOURCE_IP
                    && (dbProtocol == HTTP)) {
                throw new BadRequestException(sipErrMsg);
            }
        }
    }

    private void verifyProtocolAndHealthMonitorType(LoadBalancer queueLb) throws ProtocolHealthMonitorMismatchException {
        if (queueLb.getHealthMonitor() != null) {
            LOG.info("Health Monitor detected. Verifying that the load balancer's protocol matches the monitor type.");
            if (queueLb.getProtocol().equals(LoadBalancerProtocol.DNS_UDP) || queueLb.getProtocol().equals(LoadBalancerProtocol.UDP) || queueLb.getProtocol().equals(LoadBalancerProtocol.UDP_STREAM)) {
                throw new ProtocolHealthMonitorMismatchException("Protocol UDP, UDP_STREAM and DNS_UDP are not allowed with health monitors. ");
            }

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

    private void verifyContentCaching(LoadBalancer queueLb) throws ProtocolHealthMonitorMismatchException, BadRequestException {
        if (queueLb.isContentCaching() != null && queueLb.isContentCaching()) {
            if (queueLb.getProtocol() != LoadBalancerProtocol.HTTP) {
                throw new BadRequestException("Content caching can only be enabled for HTTP loadbalancers.");
            }
        } else if (queueLb.isContentCaching() == null) {
            queueLb.setContentCaching(false);
        }
    }

    private void verifyTCPUDPProtocolandPort(LoadBalancer queueLb, LoadBalancer dbLb) throws TCPProtocolUnknownPortException {
        if (queueLb.getProtocol() != null && (queueLb.getProtocol().equals(LoadBalancerProtocol.TCP) || queueLb.getProtocol().equals(LoadBalancerProtocol.TCP_CLIENT_FIRST)) || (queueLb.getProtocol().equals(LoadBalancerProtocol.UDP) || (queueLb.getProtocol().equals(LoadBalancerProtocol.UDP_STREAM)))) {
            LOG.info("TCP and UDP Protocol detected. Port must exists");
            if (queueLb.getPort() == null) {
                throw new TCPProtocolUnknownPortException("Must provide port for TCP and UDP protocols.");
            }
        }
    }

    private void verifyTCPUDPProtocolandPort(LoadBalancer queueLb) throws TCPProtocolUnknownPortException {
        verifyTCPUDPProtocolandPort(queueLb, null);
    }

    private void verifyHalfCloseSupport(LoadBalancer lb, boolean isHalfClose) throws BadRequestException {
        if (isHalfClose) {
            if (lb.getProtocol() != null && !(lb.getProtocol().equals(LoadBalancerProtocol.TCP) || lb.getProtocol().equals(LoadBalancerProtocol.TCP_CLIENT_FIRST))) {
                LOG.debug("TCP or TCP_CLIENT_FIRST Protocol only allowed with Half Close Support and will not be enabled at this time. ");
                throw new BadRequestException("Must provide valid protocol for half close support, please view documentation for more details. ", new Exception("Half Close support and Load Balancer protocol not valid. "));
            }
            LOG.debug("Half Close support will be enabled for load balancer ");
        }
    }

    private void verifyHalfCloseSupport(LoadBalancer lb) throws BadRequestException {
        if (lb.isHalfClosed() != null) {
            verifyHalfCloseSupport(lb, lb.isHalfClosed());
        }
    }

    private void setHostForNewLoadBalancer(LoadBalancer loadBalancer) throws EntityNotFoundException, UnprocessableEntityException, ClusterStatusException, BadRequestException {
        boolean isHost = false;
        LoadBalancer gLb = new LoadBalancer();

        Integer vipId = null;
        try {
            for (LoadBalancerJoinVip loadBalancerJoinVip : loadBalancer.getLoadBalancerJoinVipSet()) {
                if (loadBalancerJoinVip.getVirtualIp().getId() != null) {
                    isHost = true;
                    vipId = loadBalancerJoinVip.getVirtualIp().getId();
                    gLb = virtualIpRepository.getLoadBalancersByVipId(vipId).iterator().next();
                }
            }

            for (LoadBalancerJoinVip6 loadBalancerJoinVip6 : loadBalancer.getLoadBalancerJoinVip6Set()) {
                if (loadBalancerJoinVip6.getVirtualIp().getId() != null) {
                    isHost = true;
                    vipId = loadBalancerJoinVip6.getVirtualIp().getId();
                    gLb = virtualIpv6Repository.getLoadBalancersByVipId(vipId).iterator().next();

                }
            }
        } catch (NoSuchElementException nse) {
            LOG.info(String.format("Virtual ip id provided was not valid. for Account: %s LoadBalancer %s VIPID: %s", loadBalancer.getAccountId(), loadBalancer.getId(), vipId));
            throw new BadRequestException("Shared virtual ip could not be found. Please provide a valid virtual ip id to process this request.");
        }

        if (!isHost) {
            loadBalancer.setHost(hostService.getDefaultActiveHostAndActiveCluster());
        } else {
            if (gLb != null) {
                loadBalancer.setHost(gLb.getHost());
            }
        }
    }

    private void setVipConfigForLoadBalancer(LoadBalancer lbFromApi) throws OutOfVipsException, AccountMismatchException, UniqueLbPortViolationException, EntityNotFoundException, BadRequestException, ImmutableEntityException, UnprocessableEntityException {

        if (!lbFromApi.getLoadBalancerJoinVipSet().isEmpty()) {
            if (lbFromApi.getLoadBalancerJoinVipSet().size() > 1) {
                throw new BadRequestException("Cannot supply more than one IPV4 virtual ip.");
            }
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
                    newVipConfig.addAll(getSharedIpv4Vips(loadBalancerJoinVip.getVirtualIp(), vipsOnAccount, lbFromApi));
                }
            }
            lbFromApi.setLoadBalancerJoinVipSet(newVipConfig);
        }

        if (!lbFromApi.getLoadBalancerJoinVip6Set().isEmpty()) {
            if (lbFromApi.getLoadBalancerJoinVip6Set().size() > 1) {
                throw new BadRequestException("Cannot supply more than one IPV6 virtual ip");
            }
            Set<LoadBalancerJoinVip6> newVip6Config = new HashSet<LoadBalancerJoinVip6>();
            List<VirtualIpv6> vips6OnAccount = virtualIpv6Repository.getVips6ByAccountId(lbFromApi.getAccountId());
            Set<LoadBalancerJoinVip6> loadBalancerJoinVip6SetConfig = lbFromApi.getLoadBalancerJoinVip6Set();
            lbFromApi.setLoadBalancerJoinVip6Set(null);
            for (LoadBalancerJoinVip6 loadBalancerJoinVip6 : loadBalancerJoinVip6SetConfig) {
                if (loadBalancerJoinVip6.getVirtualIp().getId() == null) {
                    VirtualIpv6 ipv6 = virtualIpService.allocateIpv6VirtualIp(lbFromApi);
                    LoadBalancerJoinVip6 lbjv6 = new LoadBalancerJoinVip6();
                    lbjv6.setVirtualIp(ipv6);
                    newVip6Config.add(lbjv6);
                } else {
                    //share ipv6 vip here..
                    newVip6Config.addAll(getSharedIpv6Vips(loadBalancerJoinVip6.getVirtualIp(), vips6OnAccount, lbFromApi));
                }
                lbFromApi.setLoadBalancerJoinVip6Set(newVip6Config);
            }
        }
    }

    private Set<LoadBalancerJoinVip> getSharedIpv4Vips(VirtualIp vipConfig, List<VirtualIp> vipsOnAccount, LoadBalancer loadBalancer) throws AccountMismatchException, UniqueLbPortViolationException, BadRequestException {
        Set<LoadBalancerJoinVip> sharedVips = new HashSet<LoadBalancerJoinVip>();
        boolean belongsToProperAccount = false;
        String uniqueMsg = "Another load balancer is currently using the requested port with the shared virtual ip.";

        // Verify this is a valid virtual ip to share
        for (VirtualIp vipOnAccount : vipsOnAccount) {
            if (vipOnAccount.getId().equals(vipConfig.getId())) {
                if (virtualIpService.isIpv4VipPortCombinationInUse(vipOnAccount, loadBalancer.getPort())) {
                    if (!checkLBProtocol(loadBalancer)) {
                        throw new UniqueLbPortViolationException(uniqueMsg);
                    } else {
                        if (!verifySharedVipProtocols(vipOnAccount, loadBalancer)) {
                            throw new BadRequestException("The requesting load balancer is in conflict with the shared virtual ip port/protocol combination. Please refer to documentation for more info.");
                        }

                    }
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

    private Set<LoadBalancerJoinVip6> getSharedIpv6Vips(VirtualIpv6 vipConfig, List<VirtualIpv6> vipsOnAccount, LoadBalancer loadBalancer) throws AccountMismatchException, UniqueLbPortViolationException, BadRequestException {
        Set<LoadBalancerJoinVip6> sharedVips = new HashSet<LoadBalancerJoinVip6>();
        boolean belongsToProperAccount = false;
        String uniqueMsg = "Another load balancer is currently using the requested port with the shared virtual ip.";

        // Verify this is a valid virtual ip to share
        for (VirtualIpv6 vipOnAccount : vipsOnAccount) {
            if (vipOnAccount.getId().equals(vipConfig.getId())) {
                if (virtualIpService.isIpv6VipPortCombinationInUse(vipOnAccount, loadBalancer.getPort())) {
                    if (!checkLBProtocol(loadBalancer)) {
                        throw new UniqueLbPortViolationException(uniqueMsg);
                    } else {
                        if (!verifySharedVip6Protocols(vipOnAccount, loadBalancer)) {
                            throw new BadRequestException("The requesting load balancer is in conflict with the shared virtual ip port/protocol combination. Please refer to documentation for more info.");
                        }
                    }
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

    private boolean checkLBProtocol(LoadBalancer loadBalancer) {
        return loadBalancer.getProtocol() == LoadBalancerProtocol.TCP || loadBalancer.getProtocol() == LoadBalancerProtocol.DNS_TCP
                || loadBalancer.getProtocol() == LoadBalancerProtocol.DNS_UDP || loadBalancer.getProtocol() == LoadBalancerProtocol.UDP || loadBalancer.getProtocol() == LoadBalancerProtocol.UDP_STREAM || loadBalancer.getProtocol() == LoadBalancerProtocol.TCP_CLIENT_FIRST;
    }

    private boolean verifySharedVipProtocols(VirtualIp vip, LoadBalancer loadBalancer) {
        return verifySharedVipProtocols(virtualIpService.getLoadBalancerByVipId(vip.getId()), loadBalancer);
    }

    private boolean verifySharedVip6Protocols(VirtualIpv6 vip6, LoadBalancer loadBalancer) {
        return verifySharedVipProtocols(virtualIpv6Repository.getLoadBalancersByVipId(vip6.getId()), loadBalancer);
    }

    private boolean verifySharedVipProtocols(List<LoadBalancer> sharedLbs, LoadBalancer loadBalancer) {
        int invalidProtos = 0;

        for (LoadBalancer lb : sharedLbs) {
            if (!checkLBProtocol(lb)) {
                invalidProtos++;
            } else {
                if (!verifyProtoGroups(lb, loadBalancer)) {
                    invalidProtos++;
                }
            }
        }
        return invalidProtos < 1;
    }

    private boolean verifyProtoGroups(LoadBalancer lbToCheck, LoadBalancer lbBeingShared) {
        if ((lbBeingShared.getProtocol() == DNS_TCP && lbToCheck.getProtocol() == DNS_UDP)
                || (lbBeingShared.getProtocol() == DNS_UDP && lbToCheck.getProtocol() == DNS_TCP)) {
            return true;
        } else if (lbBeingShared.getProtocol() == UDP_STREAM
                && (lbToCheck.getProtocol() == LoadBalancerProtocol.TCP || lbToCheck.getProtocol() == LoadBalancerProtocol.TCP_CLIENT_FIRST)) {
            return true;
        } else if (lbBeingShared.getProtocol() == LoadBalancerProtocol.UDP
                && (lbToCheck.getProtocol() == LoadBalancerProtocol.TCP || lbToCheck.getProtocol() == LoadBalancerProtocol.TCP_CLIENT_FIRST)) {
            return true;
        } else if (lbToCheck.getProtocol() == UDP_STREAM
                && (lbBeingShared.getProtocol() == LoadBalancerProtocol.TCP || lbBeingShared.getProtocol() == LoadBalancerProtocol.TCP_CLIENT_FIRST)) {
            return true;
        } else if (lbToCheck.getProtocol() == LoadBalancerProtocol.UDP
                && (lbBeingShared.getProtocol() == LoadBalancerProtocol.TCP || lbBeingShared.getProtocol() == LoadBalancerProtocol.TCP_CLIENT_FIRST)) {
            return true;
        }

        return false;
    }

    @Transactional
    private void joinIpv6OnLoadBalancer(LoadBalancer lb) {
        Set<LoadBalancerJoinVip6> loadBalancerJoinVip6SetConfig = lb.getLoadBalancerJoinVip6Set();
        lb.setLoadBalancerJoinVip6Set(null);
        Set<LoadBalancerJoinVip6> newLbVip6Setconfig = new HashSet<LoadBalancerJoinVip6>();
        lb.setLoadBalancerJoinVip6Set(newLbVip6Setconfig);
        for (LoadBalancerJoinVip6 jv6 : loadBalancerJoinVip6SetConfig) {
            LoadBalancerJoinVip6 jv = new LoadBalancerJoinVip6(lb.getPort(), lb, jv6.getVirtualIp());
            virtualIpRepository.persist(jv);
        }
    }

    @Override
    public SessionPersistence getSessionPersistenceByAccountIdLoadBalancerId(Integer accountId, Integer loadbalancerId) throws EntityNotFoundException, DeletedStatusException, BadRequestException {
        return loadBalancerRepository.getSessionPersistenceByAccountIdLoadBalancerId(accountId, loadbalancerId);
    }

    @Override
    @Transactional
    public List<LoadBalancer> reassignLoadBalancerHost(List<LoadBalancer> lbs) throws Exception {
        List<LoadBalancer> invalidLbs = new ArrayList<LoadBalancer>();
        List<LoadBalancer> validLbs = new ArrayList<LoadBalancer>();
        LoadBalancer dbLb;

        List<LoadBalancer> lbsNeededForSharedVips = verifySharedVipsOnLoadBalancers(lbs);
        if (lbsNeededForSharedVips.size() > 0) {
            String[] sharedVipLBArray = buildLbArray(lbsNeededForSharedVips);
            throw new BadRequestException("Found LoadBalancer sharing virtual ips. LoadBalancers: " + StringUtilities.buildDelemtedListFromStringArray(sharedVipLBArray, ",") + " are missing, please include the missing load balancers and retry the request.");
        }

        for (LoadBalancer lb : lbs) {
            dbLb = loadBalancerRepository.getById(lb.getId());
            if (dbLb.isSticky()) {
                invalidLbs.add(dbLb);
            } else {
                processSpecifiedOrDefaultHost(lb);
                validLbs.add(lb);
            }
        }

        if (!invalidLbs.isEmpty()) {
            String[] invalidLbArray = buildLbArray(invalidLbs);
            throw new BadRequestException("Found sticky LoadBalancers: " + StringUtilities.buildDelemtedListFromStringArray(invalidLbArray, ",") + " please remove and retry the request");
        }

        //Everythings ok, begin update...
        for (LoadBalancer lb : validLbs) {
            setStatus(lb, LoadBalancerStatus.PENDING_UPDATE);
//            loadBalancerRepository.save(lb);
        }

        return validLbs;
    }

    private void processSpecifiedOrDefaultHost(LoadBalancer lb) throws EntityNotFoundException, BadRequestException, ClusterStatusException {
        Integer hostId = null;
        Host specifiedHost;

        if (lb.getHost() != null) {
            hostId = lb.getHost().getId();
        }
        if (!lb.isSticky()) {
            if (hostId != null) {
                specifiedHost = hostService.getById(hostId);
                if (!(specifiedHost.getHostStatus().equals(HostStatus.ACTIVE) || specifiedHost.getHostStatus().equals(HostStatus.ACTIVE_TARGET))) {
                    setStatus(lb, LoadBalancerStatus.ACTIVE);
                    throw new BadRequestException("Load balancers cannot move to a host(" + specifiedHost.getId() + ") that is not in ACTIVE or ACTIVE_TARGET status.");
                }
                lb.setHost(specifiedHost);
            } else {
                lb.setHost(hostService.getDefaultActiveHostAndActiveCluster());
            }
        }
    }

    @Transactional
    @Override
    public boolean setErrorPage(Integer lid, Integer accountId, String content) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException {
        if (!testAndSetStatus(accountId, lid, LoadBalancerStatus.PENDING_UPDATE)) {
            String message = "Load balancer is considered immutable and cannot process request";
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        }
        return loadBalancerRepository.setErrorPage(lid, accountId, content);
    }

    @Transactional
    @Override
    public boolean setDefaultErrorPage(String content) throws EntityNotFoundException {
        return loadBalancerRepository.setDefaultErrorPage(content);
    }

    @Transactional
    @Override
    public boolean removeErrorPage(Integer lid, Integer accountId) throws EntityNotFoundException, UnprocessableEntityException, ImmutableEntityException {
        if (!testAndSetStatus(accountId, lid, LoadBalancerStatus.PENDING_UPDATE)) {
            String message = "Load balancer is considered immutable and cannot process request";
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        }
        return loadBalancerRepository.removeErrorPage(lid, accountId);
    }

    @Transactional
    @Override
    public List<LoadBalancer> getLoadBalancersWithNode(String nodeAddress, Integer accountId) {
        List<LoadBalancer> retLbs = loadBalancerRepository.getAllWithNode(nodeAddress, accountId);
        List<LoadBalancer> domainLbs = new ArrayList<LoadBalancer>();
        for (LoadBalancer loadbalancer : retLbs) {
            LoadBalancer lb = new LoadBalancer();
            lb.setName(loadbalancer.getName());
            lb.setId(loadbalancer.getId());
            lb.setStatus(loadbalancer.getStatus());
            domainLbs.add(loadbalancer);
        }
        return domainLbs;
    }

    @Override
    public List<LoadBalancer> getLoadBalancersWithUsage(Integer accountId, Calendar startTime, Calendar endTime, Integer offset, Integer limit) {
        List<LoadBalancer> domainLbs;
        domainLbs = loadBalancerRepository.getLoadBalancersActiveInRange(accountId, startTime, endTime, offset, limit);
        return domainLbs;
    }

    @Override
    public boolean isServiceNetLoadBalancer(Integer lbId) {
        try {
            final Set<VirtualIp> vipsByAccountIdLoadBalancerId = loadBalancerRepository.getVipsByLbId(lbId);

            for (VirtualIp virtualIp : vipsByAccountIdLoadBalancerId) {
                if (virtualIp.getVipType().equals(VirtualIpType.SERVICENET)) return true;
            }

        } catch (EntityNotFoundException e) {
            return false;
        } catch (DeletedStatusException e) {
            return false;
        }

        return false;
    }

    @Override
    public BitTags getCurrentBitTags(Integer lbId) {
        BitTags bitTags = new BitTags();

        try {
            SslTermination sslTerm = sslTerminationRepository.getSslTerminationByLbId(lbId);

            if (sslTerm.isEnabled()) {
                bitTags.flipTagOn(BitTag.SSL);
                if (!sslTerm.isSecureTrafficOnly()) {
                    bitTags.flipTagOn(BitTag.SSL_MIXED_MODE);
                }
            }

        } catch (EntityNotFoundException e1) {
            bitTags.flipTagOff(BitTag.SSL);
            bitTags.flipTagOff(BitTag.SSL_MIXED_MODE);
        }

        if (isServiceNetLoadBalancer(lbId)) {
            bitTags.flipTagOn(BitTag.SERVICENET_LB);
        }

        return bitTags;
    }

    private List<LoadBalancer> verifySharedVipsOnLoadBalancers(List<LoadBalancer> lbs) throws EntityNotFoundException, BadRequestException {
        List<LoadBalancer> lbsWithSharedVips = new ArrayList<LoadBalancer>();
        List<LoadBalancer> lbsNeededForRequest = new ArrayList<LoadBalancer>();

        for (LoadBalancer lb : lbs) {
            LoadBalancer dbLb = loadBalancerRepository.getById(lb.getId());

            Set<LoadBalancerJoinVip> vip4Set = dbLb.getLoadBalancerJoinVipSet();
            for (LoadBalancerJoinVip lbjv : vip4Set) {
                List<LoadBalancer> lbsSharingVip4 = virtualIpRepository.getLoadBalancersByVipId(lbjv.getVirtualIp().getId());
                lbsWithSharedVips.addAll(lbsSharingVip4);
            }
            Set<LoadBalancerJoinVip6> vip6Set = dbLb.getLoadBalancerJoinVip6Set();
            for (LoadBalancerJoinVip6 lbjv : vip6Set) {
                List<LoadBalancer> lbsSharingVip6 = virtualIpRepository.getLoadBalancersByVipId(lbjv.getVirtualIp().getId());
                lbsWithSharedVips.addAll(lbsSharingVip6);
            }
        }

        if (lbsWithSharedVips.size() > 0) {
            for (LoadBalancer lbsv : lbsWithSharedVips) {
                if (!buildLbIdList(lbs).contains(lbsv.getId())) {
                    lbsNeededForRequest.add(lbsv);
                }
            }
        }

        return lbsNeededForRequest;
    }

    private String[] buildLbArray(List<LoadBalancer> loadBalancers) {
        String[] loadbalancersArray = new String[loadBalancers.size()];
        for (int i = 0; i < loadBalancers.size(); i++) {
            loadbalancersArray[i] = loadBalancers.get(i).getId().toString();
        }
        return loadbalancersArray;
    }

    private List<Integer> buildLbIdList(List<LoadBalancer> loadBalancers) {
        List<Integer> incommingLbIds = new ArrayList<Integer>();
        for (LoadBalancer lb : loadBalancers) {
            incommingLbIds.add(lb.getId());
        }
        return incommingLbIds;
    }
}
