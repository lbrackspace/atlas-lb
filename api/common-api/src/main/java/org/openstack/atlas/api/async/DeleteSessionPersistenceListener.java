package org.openstack.atlas.api.async;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.SessionPersistence;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Message;

import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.ZEUS_FAILURE;
import static org.openstack.atlas.service.domain.events.entities.CategoryType.DELETE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.DELETE_SESSION_PERSISTENCE;

public class DeleteSessionPersistenceListener extends BaseListener {

    private final Log LOG = LogFactory.getLog(DeleteSessionPersistenceListener.class);

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);
        LoadBalancer requestLb = getLoadbalancerFromMessage(message);
        LoadBalancer dbLoadBalancer;

        try {
            dbLoadBalancer = loadBalancerService.get(requestLb.getId(), requestLb.getAccountId());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", requestLb.getId());
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(requestLb.getAccountId(), requestLb.getId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(requestLb);
            return;
        }

        try {
            LOG.debug(String.format("Removing session persistence for load balancer '%d' in Zeus...", requestLb.getId()));
            reverseProxyLoadBalancerService.removeSessionPersistence(requestLb.getId(), requestLb.getAccountId());
            LOG.debug(String.format("Successfully removed session persistence for load balancer '%d' in Zeus.", requestLb.getId()));
        } catch (Exception e) {
            loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error removing session persistence in Zeus for loadbalancer '%d'.", requestLb.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(requestLb.getAccountId(), requestLb.getId(), e, ZEUS_FAILURE.name(), alertDescription);
            sendErrorToEventResource(requestLb);

            return;
        }

        // Update load balancer in DB
        dbLoadBalancer.setSessionPersistence(SessionPersistence.NONE);
        dbLoadBalancer.setStatus(LoadBalancerStatus.ACTIVE);
        loadBalancerService.update(dbLoadBalancer);

        //Set status record
        loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.ACTIVE);

        // Add atom entry
        String atomTitle = "Session Persistence Successfully Deleted";
        String atomSummary = "Session persistence successfully deleted";
        notificationService.saveSessionPersistenceEvent(requestLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), atomTitle, atomSummary, DELETE_SESSION_PERSISTENCE, DELETE, INFO);

        LOG.info(String.format("Delete session persistence operation complete for load balancer '%d'.", requestLb.getId()));
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Deleting Session Persistence";
        String desc = "Could not delete the session persistence settings at this time.";
        notificationService.saveSessionPersistenceEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, DELETE_SESSION_PERSISTENCE, DELETE, CRITICAL);
    }
}
