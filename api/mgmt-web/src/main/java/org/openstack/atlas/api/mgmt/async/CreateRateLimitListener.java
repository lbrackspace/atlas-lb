package org.openstack.atlas.api.mgmt.async;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.events.entities.CategoryType;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Message;

import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.ACTIVE;
import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.ERROR;
import static org.openstack.atlas.service.domain.events.entities.CategoryType.CREATE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.LBDEVICE_FAILURE;

public class CreateRateLimitListener extends BaseListener {

    final Log LOG = LogFactory.getLog(CreateRateLimitListener.class);

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
            LOG.debug("Creating rate limit in LB Device...");
            reverseProxyLoadBalancerService.setRateLimit(queueLb.getId(), queueLb.getAccountId(), queueLb.getRateLimit());
            LOG.debug("Successfully created rate limit in LB Device.");
        } catch (Exception e) {
            String alertDescription = String.format("Error adding rate limit for load balancer '%d'", queueLb.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(queueLb.getAccountId(), queueLb.getId(), e, LBDEVICE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);
            return;
        }

        // Update load balancer in DB
        loadBalancerService.setStatus(dbLoadBalancer, ACTIVE);

        // Add atom entry
        String atomTitle = "Rate Limit Successfully Created";
        String atomSummary = createAtomSummary(queueLb).toString();
        notificationService.saveLoadBalancerEvent(queueLb.getUserName(), queueLb.getAccountId(), queueLb.getId(), atomTitle, atomSummary, EventType.UPDATE_LOADBALANCER, CREATE, INFO);

        LOG.info(String.format("Rate limit successfully created for load balancer '%d'.", queueLb.getId()));
    }

    @Override
    public void onRollback(final Message message, final Exception e) {
        try {
            // TODO: Guess were not using this, ill leave it here until verified... 
            LOG.error("An exception has occurred. Putting loadbalancer in error state...", e);
            LoadBalancer loadBalancer = getLoadbalancerFromMessage(message);
            LoadBalancer dbLoadBalancer = loadBalancerService.get(loadBalancer.getId(), loadBalancer.getAccountId());
            dbLoadBalancer.setStatus(ERROR);
            loadBalancerService.update(dbLoadBalancer);
        } catch (Exception e1) {
            LOG.error("Exception occurred during rollback ", e1);
        }
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Deleting Access List";
        String desc = "Could not delete the access list at this time";
        notificationService.saveLoadBalancerEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, EventType.UPDATE_LOADBALANCER, CategoryType.DELETE, CRITICAL);
    }

    private StringBuffer createAtomSummary(LoadBalancer lb) {
        StringBuffer atomSummary = new StringBuffer();
        atomSummary.append("Rate limit successfully created with ");
        atomSummary.append("maxRequestsPerSecond: '").append(lb.getRateLimit().getMaxRequestsPerSecond()).append("', ");
        atomSummary.append("expirationTime: '").append(lb.getRateLimit().getExpirationTime().getTime()).append("', ");
        atomSummary.append("ticketId: '").append(lb.getRateLimit().getTicket().getTicketId()).append("'");
        return atomSummary;
    }
}
