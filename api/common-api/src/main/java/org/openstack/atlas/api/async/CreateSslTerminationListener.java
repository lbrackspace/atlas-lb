package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.atom.EntryHelper;
import org.openstack.atlas.api.helpers.NodesHelper;
import org.openstack.atlas.docs.loadbalancers.api.v1.*;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.entities.AccessList;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeStatus;
import org.openstack.atlas.service.domain.entities.SessionPersistence;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;

import javax.jms.Message;

import static org.openstack.atlas.api.atom.EntryHelper.*;
import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.ACTIVE;
import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.ERROR;
import static org.openstack.atlas.service.domain.entities.NodeStatus.OFFLINE;
import static org.openstack.atlas.service.domain.entities.NodeStatus.ONLINE;
import static org.openstack.atlas.service.domain.events.UsageEvent.SSL_ON;
import static org.openstack.atlas.service.domain.events.entities.CategoryType.CREATE;
import static org.openstack.atlas.service.domain.events.entities.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.*;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.ZEUS_FAILURE;

public class CreateSslTerminationListener extends BaseListener {
    private final Log LOG = LogFactory.getLog(CreateSslTerminationListener.class);

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);

         MessageDataContainer dataContainer = getDataContainerFromMessage(message);
        SslTermination dbTermination;

        LoadBalancer dbLoadBalancer = new LoadBalancer();

        try {
            dbLoadBalancer = loadBalancerService.get(dataContainer.getLoadBalancerId(), dataContainer.getAccountId());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", dataContainer.getLoadBalancerId());
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(dbLoadBalancer);
            return;
        }

        try {
            dbTermination = loadBalancerService.getSslTermination(dataContainer.getLoadBalancerId(), dataContainer.getAccountId());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' Ssl termination not found in database.", dataContainer.getLoadBalancerId());
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(dbLoadBalancer);
            return;
        }

        try {
            LOG.debug("Creating load balancer in Zeus...");
            reverseProxyLoadBalancerService.createSslTermination(dataContainer.getLoadBalancerId(), dataContainer.getAccountId(), dbTermination);
            LOG.debug("Successfully created a load balancer in Zeus.");
        } catch (Exception e) {
            String alertDescription = String.format("An error occurred while creating loadbalancer '%d' in Zeus.", dbTermination.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), e, ZEUS_FAILURE.name(), alertDescription);
            sendErrorToEventResource(dbLoadBalancer);
            // Notify usage processor
            notifyUsageProcessor(message, dbLoadBalancer, UsageEvent.SSL_OFF);
            return;
        }

        addAtomEntriesForSslTermination(dbLoadBalancer, dbLoadBalancer, dbTermination);
        // Notify usage processor
        notifyUsageProcessor(message, dbLoadBalancer, SSL_ON);
        LOG.info(String.format("Created load balancer ssl termination'%d' successfully for loadbalancer: ", dbLoadBalancer.getId()));
    }

    private void addAtomEntriesForSslTermination(LoadBalancer queueLb, LoadBalancer dbLoadBalancer, SslTermination sslTermination) {
            notificationService.saveSslTerminationEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), dbLoadBalancer.getConnectionLimit().getId(), CREATE_SSL_TERMINATION_TITLE, EntryHelper.createSslTerminationSummary(sslTermination), CREATE_SSL_TERMINATION, CREATE, INFO);
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Creating Load Balancer SSL Termination";
        String desc = "Could not create a load balancer SSL Termination at this time";
        notificationService.saveLoadBalancerEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, CREATE_SSL_TERMINATION, CREATE, CRITICAL);
    }
}
