package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;

import javax.jms.Message;
import javax.persistence.PersistenceException;

import static org.openstack.atlas.service.domain.events.entities.CategoryType.DELETE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.DELETE_CONNECTION_THROTTLE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.ZEUS_FAILURE;

public class DeleteConnectionThrottleListener extends BaseListener {

    private final Log LOG = LogFactory.getLog(DeleteConnectionThrottleListener.class);

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
            LOG.debug("Deleting connection throttle in Zeus...");
            reverseProxyLoadBalancerService.deleteConnectionThrottle(dbLoadBalancer);
            LOG.debug("Successfully deleted connection throttle in Zeus.");
        } catch (Exception e) {
            loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error deleting connection throttle in Zeus for loadbalancer '%d'.", queueLb.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(queueLb.getAccountId(), queueLb.getId(), e, ZEUS_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);

            return;
        }

        connectionThrottleService.delete(dbLoadBalancer);
        loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ACTIVE);

        // Add atom entry
        String atomTitle = "Connection Throttle Successfully Deleted";
        String atomSummary = "Connection throttle successfully deleted";
        try {
            //TODO: fix the event...
        notificationService.saveConnectionLimitEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), dbLoadBalancer.getConnectionLimit().getId(), atomTitle, atomSummary, DELETE_CONNECTION_THROTTLE, DELETE, INFO);
        } catch (PersistenceException pe) {
            LOG.error("Error saving the connection throttle event for load balancer: " + queueLb.getId() + "for account: " + queueLb.getAccountId());
        }

        LOG.info(String.format("Delete connection throttle operation complete for load balancer '%d'.", queueLb.getId()));
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Deleting Connection Throttle";
        String desc = "Could delete the connection throttle at this time";
        notificationService.saveConnectionLimitEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), lb.getConnectionLimit().getId(), title, desc, DELETE_CONNECTION_THROTTLE, DELETE, CRITICAL);
    }
}
