package org.openstack.atlas.api.async;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.SessionPersistence;
import org.openstack.atlas.service.domain.entities.UserPages;
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
        LoadBalancer queLb = getLoadbalancerFromMessage(message);
        LoadBalancer dbLoadBalancer;
        LoadBalancer transportLb;

        try {
            dbLoadBalancer = loadBalancerService.getWithUserPages(queLb.getId(), queLb.getAccountId());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", queLb.getId());
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(queLb.getAccountId(), queLb.getId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queLb);
            return;
        }

        try {
            if (isRestAdapter()) {
                LOG.debug(String.format("Removing session persistence for load balancer '%d' in STM...", queLb.getId()));
                queLb.setSessionPersistence(SessionPersistence.NONE);
                reverseProxyLoadBalancerStmService.updateLoadBalancer(dbLoadBalancer, queLb);
                LOG.debug(String.format("Successfully removed session persistence for load balancer '%d' in Zeus.", queLb.getId()));
            } else {
                LOG.debug(String.format("Removing session persistence for load balancer '%d' in ZXTM...", queLb.getId()));
                reverseProxyLoadBalancerService.removeSessionPersistence(queLb.getId(), queLb.getAccountId());
                LOG.debug(String.format("Successfully removed session persistence for load balancer '%d' in Zeus.", queLb.getId()));
            }
        } catch (Exception e) {
            loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error removing session persistence in Zeus for loadbalancer '%d'.", queLb.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(queLb.getAccountId(), queLb.getId(), e, ZEUS_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queLb);

            return;
        }

        // Update load balancer in DB
        dbLoadBalancer.setUserPages(null);
        dbLoadBalancer.setSessionPersistence(SessionPersistence.NONE);
        dbLoadBalancer.setStatus(LoadBalancerStatus.ACTIVE);
        loadBalancerService.update(dbLoadBalancer);

        //Set status record
        loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.ACTIVE);

        // Add atom entry
        String atomTitle = "Session Persistence Successfully Deleted";
        String atomSummary = "Session persistence successfully deleted";
        notificationService.saveSessionPersistenceEvent(queLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), atomTitle, atomSummary, DELETE_SESSION_PERSISTENCE, DELETE, INFO);

        LOG.info(String.format("Delete session persistence operation complete for load balancer '%d'.", queLb.getId()));
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Deleting Session Persistence";
        String desc = "Could not delete the session persistence settings at this time.";
        notificationService.saveSessionPersistenceEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, DELETE_SESSION_PERSISTENCE, DELETE, CRITICAL);
    }
}
