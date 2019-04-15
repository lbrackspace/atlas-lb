package org.openstack.atlas.api.async;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.atom.EntryHelper;
import org.openstack.atlas.api.helpers.SslTerminationUsage;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.UsageEventCollectionException;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.util.debug.Debug;

import javax.jms.Message;

import static org.openstack.atlas.service.domain.events.entities.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.UPDATE_SSL_TERMINATION;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.USAGE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.ZEUS_FAILURE;

public class UpdateSslTerminationListener extends BaseListener {
    private final Log LOG = LogFactory.getLog(UpdateSslTerminationListener.class);

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);

        MessageDataContainer dataContainer = getDataContainerFromMessage(message);
        ZeusSslTermination queTermination = dataContainer.getZeusSslTermination();
        LoadBalancer dbLoadBalancer = new LoadBalancer();

        try {
            LOG.debug("Grabbing loadbalancer...");
            dbLoadBalancer = loadBalancerService.getWithUserPages(dataContainer.getLoadBalancerId(), dataContainer.getAccountId());
            dbLoadBalancer.setUserName(dataContainer.getUserName());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", dataContainer.getLoadBalancerId());
            LOG.error(alertDescription, enfe);
            sendErrorToEventResourceUsingDataContainer(dataContainer);
            return;
        }

        //First pass
        List<SnmpUsage> usages = new ArrayList<SnmpUsage>();
        Map<Integer, SnmpUsage> usagesMap = new HashMap<Integer, SnmpUsage>();
        try {
            LOG.info(String.format("Collecting usage BEFORE ssl event for load balancer %s...", dbLoadBalancer.getId()));
            usages = usageEventCollection.getUsage(dbLoadBalancer);
            for (SnmpUsage usage : usages) {
                usagesMap.put(usage.getHostId(), usage);
            }
            LOG.info(String.format("Successfully collected usage BEFORE ssl event for load balancer %s", dbLoadBalancer.getId()));
        } catch (UsageEventCollectionException e) {
            LOG.error(String.format("Collection of the ssl usage event failed for " +
                    "load balancer: %s :: Exception: %s", dbLoadBalancer.getId(), e));
        }

        try {
            if (isRestAdapter()) {
                LOG.info("Updating load balancer ssl termination in STM...");
                reverseProxyLoadBalancerStmService.updateSslTermination(dbLoadBalancer, queTermination);
                LOG.debug("Successfully updated a load balancer ssl termination in Zeus.");
            } else {
                LOG.info("Updating load balancer ssl termination in ZXTM...");
                reverseProxyLoadBalancerService.updateSslTermination(dbLoadBalancer, queTermination);
            }
        } catch (Exception e) {
            String msg = Debug.getEST(e);
            LOG.error(String.format("Error updating loadbalancr %d: %s\n", dbLoadBalancer.getId(), msg));
            dbLoadBalancer.setStatus(LoadBalancerStatus.ERROR);
            String alertDescription = String.format("An error occurred while creating loadbalancer ssl termination '%d' in Zeus.:%s", dbLoadBalancer.getId(), msg);
            loadBalancerService.update(dbLoadBalancer);
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), e, ZEUS_FAILURE.name(), alertDescription);
            sendErrorToEventResource(dbLoadBalancer);
            return;
        }

        //Second pass
        List<SnmpUsage> usages2 = new ArrayList<SnmpUsage>();
        Map<Integer, SnmpUsage> usagesMap2 = new HashMap<Integer, SnmpUsage>();
        try {
            LOG.info(String.format("Collecting usage AFTER ssl event for load balancer %s...", dbLoadBalancer.getId()));
            usages2 = usageEventCollection.getUsage(dbLoadBalancer);
            for (SnmpUsage usage : usages2) {
                usagesMap2.put(usage.getHostId(), usage);
            }
            LOG.info(String.format("Successfully collected usage AFTER ssl event for load balancer %s", dbLoadBalancer.getId()));
        } catch (UsageEventCollectionException e) {
            LOG.error(String.format("Collection of the ssl usage event failed for " +
                    "load balancer: %s :: Exception: %s", dbLoadBalancer.getId(), e));
        }

        Calendar eventTime = Calendar.getInstance();

        SslTerminationUsage sslTermUsageHelper = new SslTerminationUsage();

        List<SnmpUsage> usagesToInsert = sslTermUsageHelper.getUsagesToInsert(dbLoadBalancer.getId(), dataContainer.getPreviousSslTermination(),
                                                                              queTermination.getSslTermination(), usagesMap, usagesMap2);

        // Notify usage processor
        try {
            if (queTermination.getSslTermination().getEnabled()) {
                if (queTermination.getSslTermination().getSecureTrafficOnly()) {
                    usageEventCollection.processUsageEvent(usagesToInsert, dbLoadBalancer, UsageEvent.SSL_ONLY_ON, eventTime);
                } else {
                    usageEventCollection.processUsageEvent(usagesToInsert, dbLoadBalancer, UsageEvent.SSL_MIXED_ON, eventTime);
                }
            } else {
                usageEventCollection.processUsageEvent(usagesToInsert, dbLoadBalancer, UsageEvent.SSL_OFF, eventTime);
            }
            LOG.info(String.format("Finished processing usage event for load balancer: %s", dbLoadBalancer.getId()));
        } catch (Exception exc) {
            String exceptionStackTrace = Debug.getExtendedStackTrace(exc);
            String usageAlertDescription = String.format("An error occurred while processing the usage for an event on loadbalancer %d: \n%s\n\n%s",
                                                         dbLoadBalancer.getId(), exc.getMessage(), exceptionStackTrace);
            LOG.error(usageAlertDescription);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), exc, USAGE_FAILURE.name(), usageAlertDescription);
        }
        // Update load balancer status in DB
        loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ACTIVE);

        addAtomEntriesForSslTermination(dbLoadBalancer, dbLoadBalancer.getSslTermination());

        LOG.info(String.format("Updated load balancer '%d' ssl termination successfully for loadbalancer: ", dbLoadBalancer.getId()));
    }

    private void addAtomEntriesForSslTermination(LoadBalancer dbLoadBalancer, SslTermination sslTermination) {
        notificationService.saveSslTerminationEvent(dbLoadBalancer.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), sslTermination.getId(), EntryHelper.UPDATE_SSL_TERMINATION_TITLE, EntryHelper.createSslTerminationSummary(sslTermination), UPDATE_SSL_TERMINATION, UPDATE, INFO);
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Updating Load Balancer SSL Termination";
        String desc = "Could not update a load balancer SSL Termination at this time";
        notificationService.saveLoadBalancerEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, UPDATE_SSL_TERMINATION, UPDATE, CRITICAL);
    }

    private void sendErrorToEventResourceUsingDataContainer(MessageDataContainer dataContainer) {
        String title = "Error Updating Load Balancer SSL Termination";
        String desc = "Could not update a load balancer SSL Termination at this time";
        notificationService.saveLoadBalancerEvent(dataContainer.getUserName(), dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), title, desc, UPDATE_SSL_TERMINATION, UPDATE, CRITICAL);
    }

    private List<SnmpUsage> getUsagesToInsert(Integer loadbalancerId, SslTermination fromSslTerm, SslTermination toSslTerm, Map<Integer, SnmpUsage> firstPass, Map<Integer, SnmpUsage> secondPass){
        List<SnmpUsage> retUsages;

        if (fromSslTerm.getEnabled()){
            LOG.debug(String.format("SSL Termination was previoiusly enabled for load balancer: %s", loadbalancerId));
            if (fromSslTerm.getSecureTrafficOnly()) {
                LOG.debug(String.format("Secure Traffic only was previously enabled for load balancer: %s", loadbalancerId));
                if (toSslTerm.getEnabled()) {
                    LOG.debug(String.format("SSL Termination is now enabled for load balancer: %s", loadbalancerId));
                    if (toSslTerm.getSecureTrafficOnly()) {
                        //FROM SSL ONLY TO SSL ONLY
                        LOG.debug(String.format("Secure Traffic Only is now enabled for load balancer: %s", loadbalancerId));
                        retUsages = getUsagesToInsertByStates(UsageEvent.SSL_ONLY_ON, UsageEvent.SSL_ONLY_ON, firstPass, secondPass);
                    } else {
                        //FROM SSL ONLY TO SSL MIXED
                        LOG.debug(String.format("Secure Traffic Only is now NOT enabled for load balancer: %s", loadbalancerId));
                        retUsages = getUsagesToInsertByStates(UsageEvent.SSL_ONLY_ON, UsageEvent.SSL_MIXED_ON, firstPass, secondPass);
                    }
                } else {
                    //FROM SSL ONLY TO SSL OFF
                    LOG.debug(String.format("SSL Termination is now NOT enabled for load balancer: %s", loadbalancerId));
                    retUsages = getUsagesToInsertByStates(UsageEvent.SSL_ONLY_ON, UsageEvent.SSL_OFF, firstPass, secondPass);
                }
            } else {
                LOG.debug(String.format("Secure Traffic only was previously NOT enabled for load balancer: %s", loadbalancerId));
                if (toSslTerm.getEnabled()) {
                    LOG.debug(String.format("SSL Termination is now enabled for load balancer: %s", loadbalancerId));
                    if (toSslTerm.getSecureTrafficOnly()) {
                        //FROM SSL MIXED TO SSL ONLY
                        LOG.debug(String.format("Secure Traffic Only is now enabled for load balancer: %s", loadbalancerId));
                        retUsages = getUsagesToInsertByStates(UsageEvent.SSL_MIXED_ON, UsageEvent.SSL_ONLY_ON, firstPass, secondPass);
                    } else {
                        //FROM SSL MIXED TO SSL MIXED
                        LOG.debug(String.format("Secure Traffic Only is now NOT enabled for load balancer: %s", loadbalancerId));
                        retUsages = getUsagesToInsertByStates(UsageEvent.SSL_MIXED_ON, UsageEvent.SSL_MIXED_ON, firstPass, secondPass);
                    }
                } else {
                    //FROM SSL MIXED TO SSL OFF
                    LOG.debug(String.format("SSL Termination is now NOT enabled for load balancer: %s", loadbalancerId));
                    retUsages = getUsagesToInsertByStates(UsageEvent.SSL_MIXED_ON, UsageEvent.SSL_OFF, firstPass, secondPass);
                }
            }
        } else {
            LOG.debug(String.format("SSL Termination was previoiusly NOT enabled for load balancer: %s", loadbalancerId));
            if (toSslTerm.getEnabled()) {
                    LOG.debug(String.format("SSL Termination is now enabled for load balancer: %s", loadbalancerId));
                    if (toSslTerm.getSecureTrafficOnly()) {
                        //FROM SSL OFF TO SSL ONLY
                        LOG.debug(String.format("Secure Traffic Only is now enabled for load balancer: %s", loadbalancerId));
                        retUsages = getUsagesToInsertByStates(UsageEvent.SSL_OFF, UsageEvent.SSL_ONLY_ON, firstPass, secondPass);
                    } else {
                        //FROM SSL OFF TO SSL MIXED
                        LOG.debug(String.format("Secure Traffic Only is now NOT enabled for load balancer: %s", loadbalancerId));
                        retUsages = getUsagesToInsertByStates(UsageEvent.SSL_OFF, UsageEvent.SSL_MIXED_ON, firstPass, secondPass);
                    }
                } else {
                    //FROM SSL OFF TO SSL OFF
                    LOG.debug(String.format("SSL Termination is now NOT enabled for load balancer: %s", loadbalancerId));
                retUsages = getUsagesToInsertByStates(UsageEvent.SSL_OFF, UsageEvent.SSL_OFF, firstPass, secondPass);
                }
        }
        return retUsages;
    }

    private List<SnmpUsage> getUsagesToInsertByStates(UsageEvent fromState, UsageEvent toState,
                                                      Map<Integer, SnmpUsage> firstPass, Map<Integer, SnmpUsage> secondPass) {
        List<SnmpUsage> retUsages = new ArrayList<SnmpUsage>();
        if (fromState == UsageEvent.SSL_ONLY_ON && toState == UsageEvent.SSL_ONLY_ON) {
            for (Integer hostId : firstPass.keySet()) {
                SnmpUsage usage1 = firstPass.get(hostId);
                SnmpUsage usage2 = secondPass.get(hostId);
                SnmpUsage usageToInsert = new SnmpUsage();
                usageToInsert.setHostId(hostId);
                usageToInsert.setLoadbalancerId(usage1.getLoadbalancerId());
                usageToInsert.setBytesIn(-1L);
                usageToInsert.setBytesOut(-1L);
                usageToInsert.setConcurrentConnections(0);
                usageToInsert.setBytesInSsl(usage2.getBytesInSsl() > usage1.getBytesInSsl() ? usage2.getBytesInSsl() : usage1.getBytesInSsl());
                usageToInsert.setBytesOutSsl(usage2.getBytesOutSsl() > usage1.getBytesOutSsl() ? usage2.getBytesOutSsl() : usage1.getBytesOutSsl());
                usageToInsert.setConcurrentConnectionsSsl(usage2.getConcurrentConnectionsSsl() > usage1.getConcurrentConnectionsSsl() ?
                                                   usage2.getConcurrentConnectionsSsl() : usage1.getConcurrentConnectionsSsl());
                retUsages.add(usageToInsert);
            }
        } else if (fromState == UsageEvent.SSL_ONLY_ON && toState == UsageEvent.SSL_MIXED_ON) {
            for (Integer hostId : firstPass.keySet()) {
                SnmpUsage usage1 = firstPass.get(hostId);
                SnmpUsage usage2 = secondPass.get(hostId);
                SnmpUsage usageToInsert = new SnmpUsage();
                usageToInsert.setHostId(hostId);
                usageToInsert.setLoadbalancerId(usage1.getLoadbalancerId());
                usageToInsert.setBytesIn(0L);
                usageToInsert.setBytesOut(0L);
                usageToInsert.setConcurrentConnections(0);
                usageToInsert.setBytesInSsl(usage2.getBytesInSsl() > usage1.getBytesInSsl() ? usage2.getBytesInSsl() : usage1.getBytesInSsl());
                usageToInsert.setBytesOutSsl(usage2.getBytesOutSsl() > usage1.getBytesOutSsl() ? usage2.getBytesOutSsl() : usage1.getBytesOutSsl());
                usageToInsert.setConcurrentConnectionsSsl(usage2.getConcurrentConnectionsSsl() > usage1.getConcurrentConnectionsSsl() ?
                                                   usage2.getConcurrentConnectionsSsl() : usage1.getConcurrentConnectionsSsl());
                retUsages.add(usageToInsert);
            }
        } else if (fromState == UsageEvent.SSL_ONLY_ON && toState == UsageEvent.SSL_OFF) {
            for (Integer hostId : firstPass.keySet()) {
                SnmpUsage usage1 = firstPass.get(hostId);
                SnmpUsage usageToInsert = new SnmpUsage();
                usageToInsert.setHostId(hostId);
                usageToInsert.setLoadbalancerId(usage1.getLoadbalancerId());
                usageToInsert.setBytesIn(0L);
                usageToInsert.setBytesOut(0L);
                usageToInsert.setConcurrentConnections(0);
                usageToInsert.setBytesInSsl(usage1.getBytesInSsl());
                usageToInsert.setBytesOutSsl(usage1.getBytesOutSsl());
                usageToInsert.setConcurrentConnectionsSsl(usage1.getConcurrentConnectionsSsl());
                retUsages.add(usageToInsert);
            }
        } else if (fromState == UsageEvent.SSL_MIXED_ON && toState == UsageEvent.SSL_ONLY_ON) {
            for (Integer hostId : firstPass.keySet()) {
                SnmpUsage usage1 = firstPass.get(hostId);
                SnmpUsage usage2 = secondPass.get(hostId);
                SnmpUsage usageToInsert = new SnmpUsage();
                usageToInsert.setHostId(hostId);
                usageToInsert.setLoadbalancerId(usage1.getLoadbalancerId());
                usageToInsert.setBytesIn(usage1.getBytesIn());
                usageToInsert.setBytesOut(usage1.getBytesOut());
                usageToInsert.setConcurrentConnections(usage1.getConcurrentConnections());
                usageToInsert.setBytesInSsl(usage2.getBytesInSsl() > usage1.getBytesInSsl() ? usage2.getBytesInSsl() : usage1.getBytesInSsl());
                usageToInsert.setBytesOutSsl(usage2.getBytesOutSsl() > usage1.getBytesOutSsl() ? usage2.getBytesOutSsl() : usage1.getBytesOutSsl());
                usageToInsert.setConcurrentConnectionsSsl(usage2.getConcurrentConnectionsSsl() > usage1.getConcurrentConnectionsSsl() ?
                                                   usage2.getConcurrentConnectionsSsl() : usage1.getConcurrentConnectionsSsl());
                retUsages.add(usageToInsert);
            }
        } else if (fromState == UsageEvent.SSL_MIXED_ON && toState == UsageEvent.SSL_MIXED_ON) {
            for (Integer hostId : firstPass.keySet()) {
                SnmpUsage usage1 = firstPass.get(hostId);
                SnmpUsage usage2 = secondPass.get(hostId);
                SnmpUsage usageToInsert = new SnmpUsage();
                usageToInsert.setHostId(hostId);
                usageToInsert.setLoadbalancerId(usage1.getLoadbalancerId());
                usageToInsert.setBytesIn(usage2.getBytesIn() > usage1.getBytesIn() ? usage2.getBytesIn() : usage1.getBytesIn());
                usageToInsert.setBytesOut(usage2.getBytesOut() > usage1.getBytesOut() ? usage2.getBytesOut() : usage1.getBytesOut());
                usageToInsert.setConcurrentConnections(usage2.getConcurrentConnections() > usage1.getConcurrentConnections() ?
                                                usage2.getConcurrentConnections() : usage1.getConcurrentConnections());
                usageToInsert.setBytesInSsl(usage2.getBytesInSsl() > usage1.getBytesInSsl() ? usage2.getBytesInSsl() : usage1.getBytesInSsl());
                usageToInsert.setBytesOutSsl(usage2.getBytesOutSsl() > usage1.getBytesOutSsl() ? usage2.getBytesOutSsl() : usage1.getBytesOutSsl());
                usageToInsert.setConcurrentConnectionsSsl(usage2.getConcurrentConnectionsSsl() > usage1.getConcurrentConnectionsSsl() ?
                                                   usage2.getConcurrentConnectionsSsl() : usage1.getConcurrentConnectionsSsl());
                retUsages.add(usageToInsert);
            }
        } else if (fromState == UsageEvent.SSL_MIXED_ON && toState == UsageEvent.SSL_OFF) {
            for (Integer hostId : firstPass.keySet()) {
                SnmpUsage usage1 = firstPass.get(hostId);
                SnmpUsage usage2 = secondPass.get(hostId);
                SnmpUsage usageToInsert = new SnmpUsage();
                usageToInsert.setHostId(hostId);
                usageToInsert.setLoadbalancerId(usage1.getLoadbalancerId());
                usageToInsert.setBytesIn(usage2.getBytesIn() > usage1.getBytesIn() ? usage2.getBytesIn() : usage1.getBytesIn());
                usageToInsert.setBytesOut(usage2.getBytesOut() > usage1.getBytesOut() ? usage2.getBytesOut() : usage1.getBytesOut());
                usageToInsert.setConcurrentConnections(usage2.getConcurrentConnections() > usage1.getConcurrentConnections() ?
                        usage2.getConcurrentConnections() : usage1.getConcurrentConnections());
                usageToInsert.setBytesInSsl(usage1.getBytesInSsl());
                usageToInsert.setBytesOutSsl(usage1.getBytesOutSsl());
                usageToInsert.setConcurrentConnectionsSsl(usage1.getConcurrentConnectionsSsl());
                retUsages.add(usageToInsert);
            }
        } else if (fromState == UsageEvent.SSL_OFF && toState == UsageEvent.SSL_ONLY_ON) {
            for (Integer hostId : firstPass.keySet()) {
                SnmpUsage usage1 = firstPass.get(hostId);
                SnmpUsage usageToInsert = new SnmpUsage();
                usageToInsert.setHostId(hostId);
                usageToInsert.setLoadbalancerId(usage1.getLoadbalancerId());
                usageToInsert.setBytesIn(usage1.getBytesIn());
                usageToInsert.setBytesOut(usage1.getBytesOut());
                usageToInsert.setConcurrentConnections(usage1.getConcurrentConnections());
                usageToInsert.setBytesInSsl(0L);
                usageToInsert.setBytesOutSsl(0L);
                usageToInsert.setConcurrentConnectionsSsl(0);
                retUsages.add(usageToInsert);
            }
        } else if (fromState == UsageEvent.SSL_OFF && toState == UsageEvent.SSL_MIXED_ON) {
            for (Integer hostId : firstPass.keySet()) {
                SnmpUsage usage1 = firstPass.get(hostId);
                SnmpUsage usage2 = secondPass.get(hostId);
                SnmpUsage usageToInsert = new SnmpUsage();
                usageToInsert.setHostId(hostId);
                usageToInsert.setLoadbalancerId(usage1.getLoadbalancerId());
                usageToInsert.setBytesIn(usage2.getBytesIn() > usage1.getBytesIn() ? usage2.getBytesIn() : usage1.getBytesIn());
                usageToInsert.setBytesOut(usage2.getBytesOut() > usage1.getBytesOut() ? usage2.getBytesOut() : usage1.getBytesOut());
                usageToInsert.setConcurrentConnections(usage2.getConcurrentConnections() > usage1.getConcurrentConnections() ?
                                                       usage2.getConcurrentConnections() : usage1.getConcurrentConnections());
                usageToInsert.setBytesInSsl(0L);
                usageToInsert.setBytesOutSsl(0L);
                usageToInsert.setConcurrentConnectionsSsl(0);
                retUsages.add(usageToInsert);
            }
        } else if (fromState == UsageEvent.SSL_OFF && toState == UsageEvent.SSL_OFF) {
            for (Integer hostId : firstPass.keySet()) {
                SnmpUsage usage1 = firstPass.get(hostId);
                SnmpUsage usage2 = secondPass.get(hostId);
                SnmpUsage usageToInsert = new SnmpUsage();
                usageToInsert.setHostId(hostId);
                usageToInsert.setLoadbalancerId(usage1.getLoadbalancerId());
                usageToInsert.setBytesIn(usage2.getBytesIn() > usage1.getBytesIn() ? usage2.getBytesIn() : usage1.getBytesIn());
                usageToInsert.setBytesOut(usage2.getBytesOut() > usage1.getBytesOut() ? usage2.getBytesOut() : usage1.getBytesOut());
                usageToInsert.setConcurrentConnections(usage2.getConcurrentConnections() > usage1.getConcurrentConnections() ?
                                                       usage2.getConcurrentConnections() : usage1.getConcurrentConnections());
                usageToInsert.setBytesInSsl(-1L);
                usageToInsert.setBytesOutSsl(-1L);
                usageToInsert.setConcurrentConnectionsSsl(0);
                retUsages.add(usageToInsert);
            }
        } else {
            for (Integer hostId : firstPass.keySet()) {
                SnmpUsage usage1 = firstPass.get(hostId);
                SnmpUsage usageToInsert = new SnmpUsage();
                usageToInsert.setHostId(hostId);
                usageToInsert.setLoadbalancerId(usage1.getLoadbalancerId());
                usageToInsert.setBytesIn(-1L);
                usageToInsert.setBytesOut(-1L);
                usageToInsert.setConcurrentConnections(0);
                usageToInsert.setBytesInSsl(-1L);
                usageToInsert.setBytesOutSsl(-1L);
                usageToInsert.setConcurrentConnectionsSsl(0);
                retUsages.add(usageToInsert);
            }
        }
        return retUsages;
    }
}
