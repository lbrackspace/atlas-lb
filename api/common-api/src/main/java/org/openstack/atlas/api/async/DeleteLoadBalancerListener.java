package org.openstack.atlas.api.async;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Message;

import static org.openstack.atlas.service.domain.events.entities.CategoryType.DELETE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.DELETE_LOADBALANCER;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.LBDEVICE_FAILURE;

public class DeleteLoadBalancerListener extends BaseListener {

    private final Log LOG = LogFactory.getLog(DeleteLoadBalancerListener.class);

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);

        LoadBalancer queueLb = getLoadbalancerFromMessage(message);
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
            LOG.debug(String.format("Deleting load balancer '%d' in LB Device...", dbLoadBalancer.getId()));
            reverseProxyLoadBalancerService.deleteLoadBalancer(dbLoadBalancer);
            LOG.debug(String.format("Successfully deleted load balancer '%d' in LB Device.", dbLoadBalancer.getId()));
        } catch (Exception e) {
            loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
            LOG.error(String.format("LoadBalancer status before error was: '%s'", dbLoadBalancer.getStatus()));
            String alertDescription = String.format("Error deleting loadbalancer '%d' in LB Device.", dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, LBDEVICE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);
            return;
        }

        loadBalancerService.pseudoDelete(dbLoadBalancer);

        // Add atom entry
        String atomTitle = "Load Balancer Successfully Deleted";
        String atomSummary = "Load balancer successfully deleted";
        notificationService.saveLoadBalancerEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), atomTitle, atomSummary, DELETE_LOADBALANCER, DELETE, INFO);

        // Notify usage processor with a usage event
        notifyUsageProcessor(message, dbLoadBalancer, UsageEvent.DELETE_LOADBALANCER);

        LOG.info(String.format("Load balancer '%d' successfully deleted.", dbLoadBalancer.getId()));
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Deleting Load Balancer";
        String desc = "Could not delete the load balancer at this time.";
        notificationService.saveLoadBalancerEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, DELETE_LOADBALANCER, DELETE, CRITICAL);
    }

}
