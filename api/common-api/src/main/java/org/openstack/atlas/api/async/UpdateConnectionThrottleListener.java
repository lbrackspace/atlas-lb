package org.openstack.atlas.api.async;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.util.Constants;
import org.openstack.atlas.api.atom.EntryHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Message;

import static org.openstack.atlas.service.domain.events.entities.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.UPDATE_CONNECTION_THROTTLE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.LBDEVICE_FAILURE;
import static org.openstack.atlas.api.atom.EntryHelper.UPDATE_THROTTLE_TITLE;

public class UpdateConnectionThrottleListener extends BaseListener {

    final Log LOG = LogFactory.getLog(UpdateConnectionThrottleListener.class);

    @Override
    public void doOnMessage(Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);
        LoadBalancer queueLb = getLoadbalancerFromMessage(message);
        LoadBalancer dbLoadBalancer;

        try {
            dbLoadBalancer = loadBalancerService.get(queueLb.getId(), queueLb.getAccountId());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format(Constants.LoadBalancerNotFound);
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(queueLb.getAccountId(), queueLb.getId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);
            return;
        }

        try {
            LOG.debug(String.format("Updating connection throttle for load balancer '%d' in LB Device...", dbLoadBalancer.getId()));
            reverseProxyLoadBalancerService.updateConnectionThrottle(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getConnectionLimit());
            LOG.debug(String.format("Successfully updated connection throttle for load balancer '%d' in LB Device.", dbLoadBalancer.getId()));
        } catch (Exception e) {
            loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error updating connection throttle in LB Device for loadbalancer '%d'.", dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, LBDEVICE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);
            return;
        }

        loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ACTIVE);

        // Add atom entry
        notificationService.saveConnectionLimitEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), dbLoadBalancer.getConnectionLimit().getId(), UPDATE_THROTTLE_TITLE, EntryHelper.createConnectionThrottleSummary(dbLoadBalancer), UPDATE_CONNECTION_THROTTLE, UPDATE, INFO);

        LOG.info(String.format("Update connection throttle operation complete for load balancer '%d'.", dbLoadBalancer.getId()));
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Updating Connection Throttle";
        String desc = "Could not update the connection throttle at this time";
        Integer itemId = lb.getConnectionLimit() == null ? lb.getId() : lb.getConnectionLimit().getId(); // TODO: Find a better way of dealing with null id
        notificationService.saveConnectionLimitEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), itemId, title, desc, UPDATE_CONNECTION_THROTTLE, UPDATE, CRITICAL);
    }
}
