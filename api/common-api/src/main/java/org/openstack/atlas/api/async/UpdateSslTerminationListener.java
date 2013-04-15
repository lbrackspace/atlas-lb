package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.atom.EntryHelper;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;

import javax.jms.Message;

import static org.openstack.atlas.service.domain.events.entities.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.UPDATE_SSL_TERMINATION;
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
        Long bytesOut = null;
        Long bytesIn = null;
        Integer concurrentConns = null;
        Long bytesOutSsl = null;
        Long bytesInSsl = null;
        Integer concurrentConnsSsl = null;

        try {
            LOG.debug("Grabbing loadbalancer...");
            dbLoadBalancer = loadBalancerService.get(dataContainer.getLoadBalancerId(), dataContainer.getAccountId());
            dbLoadBalancer.setUserName(dataContainer.getUserName());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", dataContainer.getLoadBalancerId());
            LOG.error(alertDescription, enfe);
            //OPS requested 11/07/12
//            notificationService.saveAlert(dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(dbLoadBalancer);
            return;
        }

        // Try to get non-ssl usage 1st pass
        try {
            bytesOut = reverseProxyLoadBalancerService.getLoadBalancerBytesOut(dbLoadBalancer, false);
        } catch (Exception e) {
            LOG.warn("Couldn't retrieve load balancer outbound bandwidth counter.");
        }

        try {
            bytesIn = reverseProxyLoadBalancerService.getLoadBalancerBytesIn(dbLoadBalancer, false);
        } catch (Exception e) {
            LOG.warn("Couldn't retrieve load balancer inbound bandwidth counter.");
        }

        try {
            concurrentConns = reverseProxyLoadBalancerService.getLoadBalancerCurrentConnections(dbLoadBalancer, false);
        } catch (Exception e) {
            LOG.warn("Couldn't retrieve load balancer concurrent connections counter.");
        }

        // Try to get ssl usage 1st pass
        try {
            bytesOutSsl = reverseProxyLoadBalancerService.getLoadBalancerBytesOut(dbLoadBalancer, true);
        } catch (Exception e) {
            LOG.warn("Couldn't retrieve load balancer outbound bandwidth counter.");
        }

        try {
            bytesInSsl = reverseProxyLoadBalancerService.getLoadBalancerBytesIn(dbLoadBalancer, true);
        } catch (Exception e) {
            LOG.warn("Couldn't retrieve load balancer inbound bandwidth counter.");
        }

        try {
            concurrentConnsSsl = reverseProxyLoadBalancerService.getLoadBalancerCurrentConnections(dbLoadBalancer, true);
        } catch (Exception e) {
            LOG.warn("Couldn't retrieve load balancer concurrent connections counter.");
        }


        //TEMP
        usageEventCollection.processUsageRecord(dbLoadBalancer, UsageEvent.SSL_ON);

        try {
            LOG.info("Updating load balancer ssl termination in Zeus...");
            reverseProxyLoadBalancerService.updateSslTermination(dbLoadBalancer, queTermination);
            LOG.debug("Successfully updated a load balancer ssl termination in Zeus.");
        } catch (Exception e) {
            dbLoadBalancer.setStatus(LoadBalancerStatus.ERROR);
            String alertDescription = String.format("An error occurred while creating loadbalancer ssl termination '%d' in Zeus.", dbLoadBalancer.getId());
            loadBalancerService.update(dbLoadBalancer);
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), e, ZEUS_FAILURE.name(), alertDescription);
            sendErrorToEventResource(dbLoadBalancer);

            return;
        }

        // Try to get non-ssl usage (2nd pass)
        try {
            if (bytesOut == null) bytesOut = reverseProxyLoadBalancerService.getLoadBalancerBytesOut(dbLoadBalancer, false);
        } catch (Exception e) {
            LOG.warn("Couldn't retrieve load balancer outbound bandwidth counter.");
        }

        try {
            if (bytesIn == null) bytesIn = reverseProxyLoadBalancerService.getLoadBalancerBytesIn(dbLoadBalancer, false);
        } catch (Exception e) {
            LOG.warn("Couldn't retrieve load balancer inbound bandwidth counter.");
        }

        try {
            if (concurrentConns == null) concurrentConns = reverseProxyLoadBalancerService.getLoadBalancerCurrentConnections(dbLoadBalancer, false);
        } catch (Exception e) {
            LOG.warn("Couldn't retrieve load balancer concurrent connections counter.");
        }

        // Try to get ssl usage (2nd pass)
        try {
            if (bytesOutSsl == null) bytesOutSsl = reverseProxyLoadBalancerService.getLoadBalancerBytesOut(dbLoadBalancer, true);
        } catch (Exception e) {
            LOG.warn("Couldn't retrieve load balancer outbound bandwidth counter.");
        }

        try {
            if (bytesInSsl == null) bytesInSsl = reverseProxyLoadBalancerService.getLoadBalancerBytesIn(dbLoadBalancer, true);
        } catch (Exception e) {
            LOG.warn("Couldn't retrieve load balancer inbound bandwidth counter.");
        }

        try {
            if (concurrentConnsSsl == null) concurrentConnsSsl = reverseProxyLoadBalancerService.getLoadBalancerCurrentConnections(dbLoadBalancer, true);
        } catch (Exception e) {
            LOG.warn("Couldn't retrieve load balancer concurrent connections counter.");
        }

        // Update load balancer status in DB
        loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ACTIVE);

        addAtomEntriesForSslTermination(dbLoadBalancer, dbLoadBalancer.getSslTermination());

        // Notify usage processor
        if (queTermination.getSslTermination().isEnabled()) {
            if(queTermination.getSslTermination().isSecureTrafficOnly()) {
                usageEventHelper.processUsageEvent(dbLoadBalancer, UsageEvent.SSL_ONLY_ON, bytesOut, bytesIn, concurrentConns, bytesOutSsl, bytesInSsl, concurrentConnsSsl);
            } else {
                usageEventHelper.processUsageEvent(dbLoadBalancer, UsageEvent.SSL_MIXED_ON, bytesOut, bytesIn, concurrentConns, bytesOutSsl, bytesInSsl, concurrentConnsSsl);
            }
        } else {
            usageEventHelper.processUsageEvent(dbLoadBalancer, UsageEvent.SSL_OFF, bytesOut, bytesIn, concurrentConns, bytesOutSsl, bytesInSsl, concurrentConnsSsl);
        }

        LOG.info(String.format("Updated load balancer '%d' ssl termination successfully for loadbalancer: ", dbLoadBalancer.getId()));
    }

    private void addAtomEntriesForSslTermination(LoadBalancer dbLoadBalancer, SslTermination sslTermination) {
        notificationService.saveSslTerminationEvent(dbLoadBalancer.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), sslTermination.getId(), "UPDATE_SSL_TERMINATION", EntryHelper.createSslTerminationSummary(sslTermination), UPDATE_SSL_TERMINATION, UPDATE, INFO);
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Updating Load Balancer SSL Termination";
        String desc = "Could not update a load balancer SSL Termination at this time";
        notificationService.saveLoadBalancerEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, UPDATE_SSL_TERMINATION, UPDATE, CRITICAL);
    }
}
