package org.openstack.atlas.api.async;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.SessionPersistence;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Message;

import static org.openstack.atlas.service.domain.events.entities.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.UPDATE_SESSION_PERSISTENCE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.ZEUS_FAILURE;
import static org.openstack.atlas.api.atom.EntryHelper.UPDATE_PERSISTENCE_TITLE;

public class UpdateSessionPersistenceListener extends BaseListener {

    private final Log LOG = LogFactory.getLog(UpdateSessionPersistenceListener.class);

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

        if (dbLoadBalancer.getSessionPersistence() != SessionPersistence.NONE) {
            try {
                LOG.debug(String.format("Updating session persistence for load balancer '%d' in Zeus...", dbLoadBalancer.getId()));
                reverseProxyLoadBalancerService.updateSessionPersistence(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getSessionPersistence());
                LOG.debug(String.format("Successfully updated session persistence for load balancer '%d' in Zeus...", dbLoadBalancer.getId()));
            } catch (Exception e) {
                loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
                String alertDescription = String.format("Error updating session persistence in Zeus for loadbalancer '%d'.", dbLoadBalancer.getId());
                LOG.error(alertDescription, e);
                notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, ZEUS_FAILURE.name(), alertDescription);
                sendErrorToEventResource(queueLb);

                return;
            }
        }

        // Update load balancer status in DB
        loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ACTIVE);

        if (dbLoadBalancer.getSessionPersistence() != SessionPersistence.NONE) {
            // Add atom entry
            String atomSummary = String.format("Session persistence successfully updated to '%s'", dbLoadBalancer.getSessionPersistence().name());
            notificationService.saveSessionPersistenceEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), UPDATE_PERSISTENCE_TITLE, atomSummary, UPDATE_SESSION_PERSISTENCE, UPDATE, INFO);
        }

        LOG.info(String.format("Update session persistence operation complete for load balancer '%d'.", dbLoadBalancer.getId()));
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Updating Session Persistence";
        String desc = "Could not update the session persistence settings at this time.";
        notificationService.saveSessionPersistenceEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, UPDATE_SESSION_PERSISTENCE, UPDATE, CRITICAL);
    }
}
