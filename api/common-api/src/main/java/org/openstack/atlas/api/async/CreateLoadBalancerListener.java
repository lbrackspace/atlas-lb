package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.atom.EntryHelper;
import org.openstack.atlas.api.helpers.NodesHelper;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.UsageEventCollectionException;

import javax.jms.Message;

import java.util.Calendar;

import static org.openstack.atlas.api.atom.EntryHelper.*;
import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.ACTIVE;
import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.ERROR;
import static org.openstack.atlas.service.domain.entities.NodeStatus.OFFLINE;
import static org.openstack.atlas.service.domain.entities.NodeStatus.ONLINE;
import static org.openstack.atlas.service.domain.events.entities.CategoryType.CREATE;
import static org.openstack.atlas.service.domain.events.entities.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.*;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.ZEUS_FAILURE;

public class CreateLoadBalancerListener extends BaseListener {

    private final Log LOG = LogFactory.getLog(CreateLoadBalancerListener.class);

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);

        LoadBalancer queueLb = getLoadbalancerFromMessage(message);
        LoadBalancer dbLoadBalancer;

        try {
            dbLoadBalancer = loadBalancerService.get(queueLb.getId(), queueLb.getAccountId());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", queueLb.getId());
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(queueLb.getAccountId(), queueLb.getId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);
            return;
        }

        try {

            LOG.debug("Creating load balancer in Zeus...");
            reverseProxyLoadBalancerService.createLoadBalancer(dbLoadBalancer);
            LOG.debug("Successfully created a load balancer in Zeus.");
        } catch (Exception e) {
            dbLoadBalancer.setStatus(ERROR);
            NodesHelper.setNodesToStatus(dbLoadBalancer, OFFLINE);

            loadBalancerService.update(dbLoadBalancer);
            String alertDescription = String.format("An error occurred while creating loadbalancer '%d' in Zeus.", dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, ZEUS_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);
            loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.ERROR);

            // Notify usage processor
            Calendar usageEventTime = Calendar.getInstance();
            try {
                usageEventCollection.processZeroUsageEvent(dbLoadBalancer, UsageEvent.CREATE_LOADBALANCER, usageEventTime);
            } catch (UsageEventCollectionException uex) {
                LOG.error(String.format("Collection and processing of the usage event failed for load balancer: %s " +
                        ":: Exception: %s", dbLoadBalancer.getId(), uex));
            }
            return;
        }

        Calendar usageEventTime = Calendar.getInstance();

        try {
            // Notify usage processor
            usageEventCollection.processZeroUsageEvent(dbLoadBalancer, UsageEvent.CREATE_LOADBALANCER, usageEventTime);
        } catch (UsageEventCollectionException uex) {
            LOG.error(String.format("Collection and processing of the usage event failed for load balancer: %s " +
                    ":: Exception: %s", dbLoadBalancer.getId(), uex));
        }

        // Update load balancer in DB
        dbLoadBalancer.setStatus(ACTIVE);
        NodesHelper.setNodesToStatus(dbLoadBalancer, ONLINE);
        dbLoadBalancer.setProvisioned(usageEventTime);
        dbLoadBalancer = loadBalancerService.update(dbLoadBalancer);

        //Set status history record
        loadBalancerStatusHistoryService.save(queueLb.getAccountId(), queueLb.getId(), LoadBalancerStatus.ACTIVE);

        addAtomEntryForLoadBalancer(queueLb, dbLoadBalancer);
        addAtomEntriesForNodes(queueLb, dbLoadBalancer);
        addAtomEntriesForVips(queueLb, dbLoadBalancer);
        addAtomEntryForHealthMonitor(queueLb, dbLoadBalancer);
        addAtomEntryForSessionPersistence(queueLb, dbLoadBalancer);
        addAtomEntryForConnectionLogging(queueLb, dbLoadBalancer);
        addAtomEntryForConnectionLimit(queueLb, dbLoadBalancer);
        addAtomEntriesForAccessList(queueLb, dbLoadBalancer);

        LOG.info(String.format("Created load balancer '%d' successfully.", dbLoadBalancer.getId()));
    }

    private void addAtomEntriesForAccessList(LoadBalancer queueLb, LoadBalancer dbLoadBalancer) {
        for (AccessList accessList : dbLoadBalancer.getAccessLists()) {
            notificationService.saveAccessListEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), accessList.getId(), UPDATE_ACCESS_LIST_TITLE, EntryHelper.createAccessListSummary(accessList), UPDATE_ACCESS_LIST, UPDATE, INFO);
        }
    }

    private void addAtomEntryForConnectionLimit(LoadBalancer queueLb, LoadBalancer dbLoadBalancer) {
        if (dbLoadBalancer.getConnectionLimit() != null) {
            notificationService.saveConnectionLimitEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), dbLoadBalancer.getConnectionLimit().getId(), UPDATE_THROTTLE_TITLE, EntryHelper.createConnectionThrottleSummary(dbLoadBalancer), UPDATE_CONNECTION_THROTTLE, UPDATE, INFO);
        }
    }

    private void addAtomEntryForConnectionLogging(LoadBalancer queueLb, LoadBalancer dbLoadBalancer) {
        String desc = "Connection logging successully set to " + dbLoadBalancer.isConnectionLogging().toString();
        notificationService.saveLoadBalancerEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), UPDATE_LOGGING_TITLE, desc, UPDATE_CONNECTION_LOGGING, UPDATE, EventSeverity.INFO);
    }

    private void addAtomEntryForSessionPersistence(LoadBalancer queueLb, LoadBalancer dbLoadBalancer) {
        if (dbLoadBalancer.getSessionPersistence() != SessionPersistence.NONE) {
            String atomSummary = String.format("Session persistence successfully updated to '%s'", dbLoadBalancer.getSessionPersistence().name());
            notificationService.saveSessionPersistenceEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), UPDATE_PERSISTENCE_TITLE, atomSummary, UPDATE_SESSION_PERSISTENCE, UPDATE, INFO);
        }
    }

    private void addAtomEntryForHealthMonitor(LoadBalancer queueLb, LoadBalancer dbLoadBalancer) {
        if (dbLoadBalancer.getHealthMonitor() != null) {
            notificationService.saveHealthMonitorEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), dbLoadBalancer.getHealthMonitor().getId(), UPDATE_MONITOR_TITLE, EntryHelper.createHealthMonitorSummary(dbLoadBalancer), UPDATE_HEALTH_MONITOR, UPDATE, INFO);
        }
    }

    private void addAtomEntriesForVips(LoadBalancer queueLb, LoadBalancer dbLoadBalancer) {
        for (LoadBalancerJoinVip loadBalancerJoinVip : dbLoadBalancer.getLoadBalancerJoinVipSet()) {
            VirtualIp vip = loadBalancerJoinVip.getVirtualIp();
            notificationService.saveVirtualIpEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), vip.getId(), CREATE_VIP_TITLE, EntryHelper.createVirtualIpSummary(vip), EventType.CREATE_VIRTUAL_IP, CREATE, INFO);
        }

        for (LoadBalancerJoinVip6 loadBalancerJoinVip6 : dbLoadBalancer.getLoadBalancerJoinVip6Set()) {
            VirtualIpv6 vip = loadBalancerJoinVip6.getVirtualIp();
            notificationService.saveVirtualIpEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), vip.getId(), CREATE_VIP_TITLE, EntryHelper.createVirtualIpSummary(vip), EventType.CREATE_VIRTUAL_IP, CREATE, INFO);
        }
    }

    private void addAtomEntriesForNodes(LoadBalancer queueLb, LoadBalancer dbLoadBalancer) {
        for (Node node : queueLb.getNodes()) {
            notificationService.saveNodeEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(),
                    node.getId(), CREATE_NODE_TITLE, EntryHelper.createNodeSummary(node), CREATE_NODE, CREATE, INFO);
        }
    }

    private void addAtomEntryForLoadBalancer(LoadBalancer queueLb, LoadBalancer dbLoadBalancer) {
        String atomTitle = "Load Balancer Successfully Created";
        String atomSummary = createAtomSummary(dbLoadBalancer).toString();
        notificationService.saveLoadBalancerEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), atomTitle, atomSummary, CREATE_LOADBALANCER, CREATE, INFO);
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Creating Load Balancer";
        String desc = "Could not create a load balancer at this time";
        notificationService.saveLoadBalancerEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, CREATE_LOADBALANCER, CREATE, CRITICAL);
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
