package org.openstack.atlas.api.async;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Message;

import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.LBDEVICE_FAILURE;
import static org.openstack.atlas.service.domain.events.entities.CategoryType.DELETE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.DELETE_NODE;

public class DeleteNodeListener extends BaseListener {

    private final Log LOG = LogFactory.getLog(DeleteNodeListener.class);

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

        Node nodeToDelete = queueLb.getNodes().iterator().next();
        for (Node node : dbLoadBalancer.getNodes()) {
            if (node.getId().equals(nodeToDelete.getId())) {
                nodeToDelete = node;
                break;
            }
        }

        try {
            LOG.debug(String.format("Removing node '%d' from load balancer '%d' in LB Device...", nodeToDelete.getId(), queueLb.getId()));
            reverseProxyLoadBalancerService.removeNode(queueLb.getId(), queueLb.getAccountId(), nodeToDelete);
            LOG.debug(String.format("Successfully removed node '%d' from load balancer '%d' in LB Device.", nodeToDelete.getId(), queueLb.getId()));
        } catch (Exception e) {
            loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error removing node '%d' in LB Device for loadbalancer '%d'.", nodeToDelete.getId(), queueLb.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(queueLb.getAccountId(), queueLb.getId(), e, LBDEVICE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb, nodeToDelete);
            return;
        }

        // Remove node from load balancer in DB
        dbLoadBalancer.getNodes().remove(nodeToDelete);

        // Update load balancer status in DB
        dbLoadBalancer.setStatus(LoadBalancerStatus.ACTIVE);
        loadBalancerService.update(dbLoadBalancer);

        // Add atom entry
        String atomTitle = "Node Successfully Deleted";
        String atomSummary = "Node successfully deleted";
        notificationService.saveNodeEvent(queueLb.getUserName(), queueLb.getAccountId(), queueLb.getId(), nodeToDelete.getId(), atomTitle, atomSummary, DELETE_NODE, DELETE, INFO);

        LOG.info(String.format("Delete node operation complete for load balancer '%d'.", queueLb.getId()));
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Deleting Node";
        String desc = "Could not delete the node at this time.";
        notificationService.saveLoadBalancerEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, DELETE_NODE, DELETE, CRITICAL);
    }

    private void sendErrorToEventResource(LoadBalancer lb, Node nodeToDelete) {
        String title = "Error Deleting Node";
        String desc = "Could not delete the node at this time.";
        notificationService.saveNodeEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), nodeToDelete.getId(), title, desc, DELETE_NODE, DELETE, CRITICAL);
    }
}
