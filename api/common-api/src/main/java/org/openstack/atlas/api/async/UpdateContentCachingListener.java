package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;

import javax.jms.Message;

import static org.openstack.atlas.service.domain.events.entities.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.ZEUS_FAILURE;

public class UpdateContentCachingListener extends BaseListener {

    final Log LOG = LogFactory.getLog(UpdateContentCachingListener.class);
    private static final String UPDATE_CCACHING_TITLE = "Update Content Caching";

    @Override
    public void doOnMessage(Message message) throws Exception {
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
            LOG.debug(String.format("Updating content caching for load balancer '%d' in Zeus...", dbLoadBalancer.getId()));
            reverseProxyLoadBalancerService.updateContentCaching(dbLoadBalancer);
            LOG.debug(String.format("Successfully updated content caching for load balancer '%d' in Zeus.", dbLoadBalancer.getId()));
        } catch (Exception e) {
            loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error updating content caching for load balancer '%d' in Zeus.", dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, ZEUS_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);
            return;
        }

        String desc = "Content caching successully set to " + dbLoadBalancer.isContentCaching().toString();
        notificationService.saveLoadBalancerEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), UPDATE_CCACHING_TITLE, desc, EventType.UPDATE_CONTENT_CACHING, UPDATE, EventSeverity.INFO);

        // Update load balancer status in DB
        loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ACTIVE);

        LOG.info("Update content caching operation complete.");
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Updating Content Caching";
        String desc = "Could not update content caching at this time.";
        notificationService.saveLoadBalancerEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, EventType.UPDATE_CONTENT_CACHING, UPDATE, CRITICAL);
    }
}
