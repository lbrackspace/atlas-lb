package org.openstack.atlas.api.mgmt.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.helpers.NodesHelper;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.UsageEventCollectionException;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.util.debug.Debug;

import javax.jms.Message;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.*;
import static org.openstack.atlas.service.domain.entities.NodeStatus.ONLINE;
import static org.openstack.atlas.service.domain.events.entities.CategoryType.CREATE;
import static org.openstack.atlas.service.domain.events.entities.CategoryType.DELETE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.CREATE_LOADBALANCER;
import static org.openstack.atlas.service.domain.events.entities.EventType.DELETE_LOADBALANCER;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.USAGE_FAILURE;

public class SyncListener extends BaseListener {

    final Log LOG = LogFactory.getLog(SyncListener.class);

    @Override
    public void doOnMessage(Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);
        LoadBalancer dbLoadBalancer;
        MessageDataContainer mdc = getDataContainerFromMessage(message);
        LoadBalancerStatus finalStatus = ACTIVE;

        try {
            dbLoadBalancer = loadBalancerService.getWithUserPages(mdc.getLoadBalancerId(), mdc.getAccountId());
        } catch (EntityNotFoundException enfe) {
            LOG.error(String.format("EntityNotFoundException thrown while attempting to sync Loadbalancer #%d: ", mdc.getLoadBalancerId()));
            return;
        }

        LOG.debug(String.format("Synchronizing load balancer #%d with database configuration", mdc.getLoadBalancerId()));

        final LoadBalancerStatus loadBalancerStatus = mdc.getStatus();

        List<SnmpUsage> usages = new ArrayList<SnmpUsage>();
        try {
            LOG.info(String.format("Collecting pre-sync usage for load balancer %s...", dbLoadBalancer.getId()));
            usages = usageEventCollection.getUsage(dbLoadBalancer);
            LOG.info(String.format("Successfully collected pre-sync usage for load balancer %s", dbLoadBalancer.getId()));
        } catch (UsageEventCollectionException e) {
            LOG.error(String.format("Collection of the pre-sync usage event failed for " +
                    "load balancer: %s :: Exception: %s", dbLoadBalancer.getId(), e));
        }


        if (loadBalancerStatus.equals(PENDING_DELETE) || loadBalancerStatus.equals(DELETED)) {
            finalStatus = DELETED;

            try {
                    LOG.debug(String.format("Deleting load balancer '%d' in backend...", dbLoadBalancer.getId()));
                    reverseProxyLoadBalancerVTMService.deleteLoadBalancer(dbLoadBalancer);
                    LOG.debug(String.format("Successfully deleted load balancer '%d' in backend.", dbLoadBalancer.getId()));
            } catch (Exception e) {
                String msg = String.format("Error deleting loadbalancer #%d in SyncListener(): ", mdc.getLoadBalancerId());
                loadBalancerService.setStatus(dbLoadBalancer, ERROR);
                notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, AlertType.ZEUS_FAILURE.name(), msg);
                LOG.error(msg, e);
            }

            loadBalancerService.pseudoDelete(dbLoadBalancer);

            if (loadBalancerStatus.equals(PENDING_DELETE)) {
                // Add atom entry
                String atomTitle = "Load Balancer Successfully Deleted";
                String atomSummary = "Load balancer successfully deleted";
                notificationService.saveLoadBalancerEvent(dbLoadBalancer.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), atomTitle, atomSummary, DELETE_LOADBALANCER, DELETE, INFO);

                // Notify usage processor
                Calendar eventTime = Calendar.getInstance();
                LOG.info(String.format("Processing DELETE_LOADBALANCER usage for load balancer %s...", dbLoadBalancer.getId()));
                try {
                    usageEventCollection.processUsageEvent(usages, dbLoadBalancer, UsageEvent.DELETE_LOADBALANCER, eventTime);
                    LOG.info(String.format("Completed processing DELETE_LOADBALANCER usage for load balancer %s", dbLoadBalancer.getId()));
                } catch (Exception exc) {
                    String exceptionStackTrace = Debug.getExtendedStackTrace(exc);
                    String usageAlertDescription = String.format("An error occurred while processing the usage for an event on loadbalancer %d: \n%s\n\n%s",
                                                                 dbLoadBalancer.getId(), exc.getMessage(), exceptionStackTrace);
                    LOG.error(usageAlertDescription);
                    notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), exc, USAGE_FAILURE.name(), usageAlertDescription);
                }

                //Set status record
                loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_DELETE);
            }
        } else {
            try {
                LOG.debug(String.format("Syncing load balancer %s setting status to PENDING_UPDATE", dbLoadBalancer.getId()));

                    LOG.debug(String.format("Updating loadbalancer: %s in backend...", dbLoadBalancer.getId()));
                    reverseProxyLoadBalancerVTMService.updateLoadBalancer(dbLoadBalancer, dbLoadBalancer);
                    LOG.debug(String.format("Successfully Updated loadbalancer: %s in backend...", dbLoadBalancer.getId()));

                LOG.debug(String.format("Sync of load balancer %s complete, updating status and saving events and usage...", dbLoadBalancer.getId()));

                if (loadBalancerStatus.equals(BUILD)) {
                    NodesHelper.setNodesToStatus(dbLoadBalancer, ONLINE);
                    dbLoadBalancer = loadBalancerService.update(dbLoadBalancer); //not sure if this is still needed

                    // Add atom entry
                    String atomTitle = "Load Balancer Successfully Created";
                    String atomSummary = createAtomSummary(dbLoadBalancer).toString();
                    notificationService.saveLoadBalancerEvent(dbLoadBalancer.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), atomTitle, atomSummary, CREATE_LOADBALANCER, CREATE, INFO);

                    // Notify old usage processor
                    Calendar eventTime = Calendar.getInstance();

                    try {
                        usageEventCollection.processZeroUsageEvent(dbLoadBalancer, UsageEvent.CREATE_LOADBALANCER, eventTime);
                    } catch (UsageEventCollectionException uex) {
                        LOG.error(String.format("Collection and processing of the usage event failed for load balancer: %s " +
                                ":: Exception: %s", dbLoadBalancer.getId(), uex));
                    } catch (Exception exc) {
                        String exceptionStackTrace = Debug.getExtendedStackTrace(exc);
                        String usageAlertDescription = String.format("An error occurred while processing the usage for an event on loadbalancer %d: \n%s\n\n%s",
                                                                     dbLoadBalancer.getId(), exc.getMessage(), exceptionStackTrace);
                        LOG.error(usageAlertDescription);
                        notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), exc, USAGE_FAILURE.name(), usageAlertDescription);
                    }
                }
            } catch (Exception e) {
                String msg = String.format("Error re-creating loadbalancer #%d in SyncListener(), " +
                        "setting status to ERROR, original status: %s:", mdc.getLoadBalancerId(), loadBalancerStatus);

                finalStatus = ERROR;
                notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, AlertType.ZEUS_FAILURE.name(), msg);
                LOG.error(msg, e);
            }

            try {
                //Now create the secure VS if ssl termination was already on the loadbalancer...
                if (dbLoadBalancer.hasSsl() && (loadBalancerStatus.equals(PENDING_UPDATE) || loadBalancerStatus.equals(ERROR))) {
                        Calendar eventTime = Calendar.getInstance();

                    try {
                        if (dbLoadBalancer.isUsingSsl()) {
                            if (dbLoadBalancer.getSslTermination().getSecureTrafficOnly()) {
                                usageEventCollection.collectUsageAndProcessUsageRecords(dbLoadBalancer, UsageEvent.SSL_ONLY_ON, eventTime);
                            } else {
                                usageEventCollection.collectUsageAndProcessUsageRecords(dbLoadBalancer, UsageEvent.SSL_MIXED_ON, eventTime);
                            }
                        } else {
                            usageEventCollection.collectUsageAndProcessUsageRecords(dbLoadBalancer, UsageEvent.SSL_OFF, eventTime);
                        }
                    } catch (Exception exc) {
                        String exceptionStackTrace = Debug.getExtendedStackTrace(exc);
                        String usageAlertDescription = String.format("An error occurred while processing the usage for an event on loadbalancer %d: \n%s\n\n%s",
                                dbLoadBalancer.getId(), exc.getMessage(), exceptionStackTrace);
                        LOG.error(usageAlertDescription);
                        notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), exc, USAGE_FAILURE.name(), usageAlertDescription);
                    }
                }
            } catch (Exception e) {
                String msg = String.format("Error re-creating ssl terminated loadbalancer #%d in SyncListener(), " +
                        "setting status to ERROR, original status: :", mdc.getLoadBalancerId(), loadBalancerStatus);
                finalStatus = ERROR;
                LOG.error(msg, e);
            }
        }

        loadBalancerService.setStatus(dbLoadBalancer, finalStatus);
        LOG.info(String.format("Sync operation complete for loadbalancer #%d ", mdc.getLoadBalancerId()));
    }

    private StringBuilder createAtomSummary(LoadBalancer lb) {
        StringBuilder atomSummary = new StringBuilder();
        atomSummary.append("Load balancer successfully created with ");
        atomSummary.append("name: '").append(lb.getName()).append("', ");
        atomSummary.append("algorithm: '").append(lb.getAlgorithm()).append("', ");
        atomSummary.append("protocol: '").append(lb.getProtocol()).append("', ");
        atomSummary.append("port: '").append(lb.getPort()).append("'");
        return atomSummary;
    }
}
