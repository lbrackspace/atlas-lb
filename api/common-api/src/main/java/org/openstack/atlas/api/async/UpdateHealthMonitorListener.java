package org.openstack.atlas.api.async;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.api.atom.EntryHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Message;

import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.LBDEVICE_FAILURE;
import static org.openstack.atlas.service.domain.events.entities.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.UPDATE_HEALTH_MONITOR;
import static org.openstack.atlas.api.atom.EntryHelper.UPDATE_MONITOR_TITLE;

public class UpdateHealthMonitorListener extends BaseListener {

    private final Log LOG = LogFactory.getLog(UpdateHealthMonitorListener.class);

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
            LOG.debug(String.format("Updating health monitor for load balancer '%d' in LB Device...", dbLoadBalancer.getId()));
            reverseProxyLoadBalancerService.updateHealthMonitor(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getHealthMonitor());
            LOG.debug(String.format("Successfully updated health monitor for load balancer '%d' in LB Device.", dbLoadBalancer.getId()));
        } catch (Exception e) {
            loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error updating health monitor in LB Device for loadbalancer '%d'.", dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, LBDEVICE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);
            return;
        }

        loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ACTIVE);

        // Add atom entry
        notificationService.saveHealthMonitorEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), dbLoadBalancer.getHealthMonitor().getId(), UPDATE_MONITOR_TITLE, EntryHelper.createHealthMonitorSummary(dbLoadBalancer), UPDATE_HEALTH_MONITOR, UPDATE, INFO);

        LOG.info(String.format("Update health monitor operation complete for load balancer '%d'.", dbLoadBalancer.getId()));
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Updating Health Monitor";
        String desc = "Could not update the health monitor at this time";
        Integer itemId = lb.getHealthMonitor() == null ? lb.getId() : lb.getHealthMonitor().getId(); // TODO: Find a better way of dealing with null id
        notificationService.saveHealthMonitorEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), itemId, title, desc, UPDATE_HEALTH_MONITOR, UPDATE, CRITICAL);
   }
}
