package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.UsageEventCollectionException;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;

import javax.jms.Message;

import java.util.Calendar;

import static org.openstack.atlas.service.domain.events.entities.CategoryType.DELETE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.DELETE_SSL_TERMINATION;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.ZEUS_FAILURE;

public class DeleteSslTerminationListener extends BaseListener {

    private final Log LOG = LogFactory.getLog(DeleteSslTerminationListener.class);

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);

        MessageDataContainer dataContainer = getDataContainerFromMessage(message);
        LoadBalancer dbLoadBalancer;

        LoadBalancer errorMsgLB = new LoadBalancer();
        errorMsgLB.setUserName(dataContainer.getUserName());
        errorMsgLB.setId(dataContainer.getLoadBalancerId());
        errorMsgLB.setAccountId(dataContainer.getAccountId());

        try {
            dbLoadBalancer = loadBalancerService.get(errorMsgLB.getId());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", errorMsgLB.getId());
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(errorMsgLB.getAccountId(), errorMsgLB.getId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(errorMsgLB);
            return;
        }

        try {
            LOG.debug(String.format("Deleting load balancer '%d' ssl termination in Zeus...", dbLoadBalancer.getId()));
            reverseProxyLoadBalancerService.removeSslTermination(dbLoadBalancer);
            LOG.debug(String.format("Successfully deleted load balancer ssl termination '%d' in Zeus.", dbLoadBalancer.getId()));
        } catch (Exception e) {
            loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
            LOG.error(String.format("LoadBalancer status before error was: '%s'", dbLoadBalancer.getStatus()));
            String alertDescription = String.format("Error deleting loadbalancer '%d' ssl termination in Zeus.", dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, ZEUS_FAILURE.name(), alertDescription);
            sendErrorToEventResource(errorMsgLB);
            Calendar eventTime = Calendar.getInstance();
            // Notify usage processor
            try {
                usageEventCollection.collectUsageAndProcessUsageRecords(dbLoadBalancer, UsageEvent.SSL_OFF, eventTime);
            } catch (UsageEventCollectionException uex) {
                LOG.error(String.format("Collection and processing of the usage event failed for load balancer: %s " +
                        ":: Exception: %s", dbLoadBalancer.getId(), uex));
            }
            return;
        }

        sslTerminationService.deleteSslTermination(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId());

        Calendar eventTime = Calendar.getInstance();
        // Notify usage processor with a usage event
        try {
            usageEventCollection.collectUsageAndProcessUsageRecords(dbLoadBalancer, UsageEvent.SSL_OFF, eventTime);
        } catch (UsageEventCollectionException uex) {
            LOG.error(String.format("Collection and processing of the usage event failed for load balancer: %s " +
                    ":: Exception: %s", dbLoadBalancer.getId(), uex));
        }

        // Update load balancer status in DB
        loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ACTIVE);

        // Add atom entry
        String atomTitle = "Load Balancer SSL Termination Successfully Deleted";
        String atomSummary = "Load balancer ssl termination successfully deleted";
        notificationService.saveLoadBalancerEvent(errorMsgLB.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), atomTitle, atomSummary, DELETE_SSL_TERMINATION, DELETE, INFO);

        LOG.info(String.format("Load balancer ssl termination '%d' successfully deleted.", dbLoadBalancer.getId()));
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Deleting Load Balancer ssl termination";
        String desc = "Could not delete the load balancer ssl termination at this time.";
        notificationService.saveLoadBalancerEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, DELETE_SSL_TERMINATION, DELETE, CRITICAL);
    }

}
