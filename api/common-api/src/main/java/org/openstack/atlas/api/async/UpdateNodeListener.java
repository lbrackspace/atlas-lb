package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;

import javax.jms.Message;

import static org.openstack.atlas.service.domain.events.entities.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.UPDATE_NODE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.ZEUS_FAILURE;

public class UpdateNodeListener extends BaseListener {

    private final Log LOG = LogFactory.getLog(UpdateNodeListener.class);

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

        Node nodeToUpdate = getNodeToUpdate(queueLb);

        try {
            LOG.info(String.format("Updating nodes for load balancer '%d' in Zeus...", dbLoadBalancer.getId()));
            String poolName = ZxtmNameBuilder.genVSName(dbLoadBalancer);
            reverseProxyLoadBalancerService.setNodes(dbLoadBalancer);
            reverseProxyLoadBalancerService.setNodesPriorities(poolName, dbLoadBalancer);
            LOG.info(String.format("Successfully updated nodes for load balancer '%d' in Zeus.", dbLoadBalancer.getId()));
        } catch (Exception e) {
            loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error updating node '%d' in Zeus for loadbalancer '%d'.", nodeToUpdate.getId(), dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, ZEUS_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb, nodeToUpdate);

            return;
        }

        // Update load balancer status in DB
        loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ACTIVE);

        // Add atom entry
        String atomTitle = "Node Successfully Updated";
        String atomSummary = createAtomSummary(nodeToUpdate).toString();
        notificationService.saveNodeEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), nodeToUpdate.getId(), atomTitle, atomSummary, UPDATE_NODE, UPDATE, INFO);

        LOG.info(String.format("Update node operation complete for load balancer '%d'.", dbLoadBalancer.getId()));
    }

    private Node getNodeToUpdate(LoadBalancer queueLb) {
        Node nodeToUpdate = null;
        for (Node node : queueLb.getNodes()) {
            if (node.isToBeUpdated()) {
                nodeToUpdate = node;
                break;
            }
        }
        return nodeToUpdate;
    }

    private StringBuilder createAtomSummary(Node node) {
        StringBuilder atomSummary = new StringBuilder();
        atomSummary.append("Node successfully updated with ");
        atomSummary.append("address: '").append(node.getIpAddress()).append("', ");
        atomSummary.append("port: '").append(node.getPort()).append("', ");
        atomSummary.append("weight: '").append(node.getWeight()).append("', ");
        atomSummary.append("condition: '").append(node.getCondition()).append("'");
        return atomSummary;
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Updating Node";
        String desc = "Could not update the node at this time.";
        notificationService.saveLoadBalancerEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, UPDATE_NODE, UPDATE, CRITICAL);
    }

    private void sendErrorToEventResource(LoadBalancer lb, Node nodeToUpdate) {
        String title = "Error Updating Node";
        String desc = "Could not update the node at this time.";
        notificationService.saveNodeEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), nodeToUpdate.getId(), title, desc, UPDATE_NODE, UPDATE, CRITICAL);
    }
}
