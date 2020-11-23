package org.openstack.atlas.api.mgmt.async;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Message;

import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.ZEUS_FAILURE;
import static org.openstack.atlas.service.domain.events.entities.CategoryType.DELETE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.UPDATE_LOADBALANCER;

public class DeleteRateLimitListener extends BaseListener {

    private final Log LOG = LogFactory.getLog(DeleteRateLimitListener.class);

    @Override
    public void doOnMessage(Message message) throws Exception {
        LOG.info("Entering " + getClass());
        LOG.info(message);
        LoadBalancer queueLb = getEsbRequestFromMessage(message).getLoadBalancer();
        LoadBalancer dbLoadBalancer;

        try {
            dbLoadBalancer = loadBalancerService.get(queueLb.getId());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", queueLb.getId());
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(queueLb.getAccountId(), queueLb.getId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);
            return;
        }

        try {
                LOG.debug("Deleting rate limit in backend...");
                reverseProxyLoadBalancerVTMService.deleteRateLimit(dbLoadBalancer);
                LOG.debug("Successfully deleted rate limit in STM.");

        } catch (Exception e) {
            loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);

            String alertDescription = String.format("Error deleting rate limit in STM for loadbalancer '%d'.", queueLb.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, ZEUS_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);

            return;
        }

        // Remove rate limit from load balancer in DB
        rateLimitingService.pseudoDelete(dbLoadBalancer);
        // Update load balancer in DB
        loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ACTIVE);


        // Add atom entry
        String atomTitle = "Rate Limit Successfully Deleted";
        String atomSummary = "Rate limit successfully deleted";
        notificationService.saveLoadBalancerEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), atomTitle, atomSummary, UPDATE_LOADBALANCER, DELETE, INFO);

        LOG.info(String.format("Delete rate limit operation complete for load balancer '%d'.", queueLb.getId()));
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Deleting Rate Limit";
        String desc = "Could not delete the rate limit at this time.";
        notificationService.saveLoadBalancerEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, UPDATE_LOADBALANCER, DELETE, CRITICAL);
    }
}
