package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.atom.EntryHelper;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.UsageEventCollectionException;
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

        // Notify usage processor
        try {
            if (queTermination.getSslTermination().isEnabled()) {
                LOG.debug(String.format("SSL Termination is enabled for load balancer: %s", dbLoadBalancer.getId()));
                if (queTermination.getSslTermination().isSecureTrafficOnly()) {
                    LOG.debug(String.format("SSL Termination is Secure Traffic Only for load balancer: %s", dbLoadBalancer.getId()));
                    usageEventCollection.processUsageRecord(dbLoadBalancer, UsageEvent.SSL_ONLY_ON);
                } else {
                    LOG.debug(String.format("SSL Termination is Mixed Traffic for load balancer: %s", dbLoadBalancer.getId()));
                    usageEventCollection.processUsageRecord(dbLoadBalancer, UsageEvent.SSL_MIXED_ON);
                }
            } else {
                LOG.debug(String.format("SSL Termination is NOT Enabled for load balancer: %s", dbLoadBalancer.getId()));
                usageEventCollection.processUsageRecord(dbLoadBalancer, UsageEvent.SSL_OFF);
            }
            LOG.info(String.format("Finished processing usage event for load balancer: %s", dbLoadBalancer.getId()));
        } catch (UsageEventCollectionException uex) {
            LOG.error(String.format("Collection and processing of the usage event failed for load balancer: %s " +
                    ":: Exception: %s", dbLoadBalancer.getId(), uex));
        }

        // Update load balancer status in DB
        loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ACTIVE);

        addAtomEntriesForSslTermination(dbLoadBalancer, dbLoadBalancer.getSslTermination());

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
