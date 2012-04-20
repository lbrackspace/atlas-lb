package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.util.converters.StringConverter;

import javax.jms.Message;
import java.util.List;
import java.util.Set;

import static org.openstack.atlas.service.domain.events.entities.CategoryType.DELETE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.DELETE_NODE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.ZEUS_FAILURE;

public class DeleteNodesListener extends BaseListener {

    private final Log LOG = LogFactory.getLog(DeleteNodesListener.class);

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);
        MessageDataContainer msg = getDataContainerFromMessage(message);
        LoadBalancer dbLoadBalancer;
        try {
            dbLoadBalancer = loadBalancerService.get(msg.getLoadBalancerId(), msg.getAccountId());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", msg.getLoadBalancerId());
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(msg.getAccountId(), msg.getLoadBalancerId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(msg);
            return;
        }

        List<Integer> doomedNodeIds = msg.getIds();
        List<Node> doomedNodes = nodeService.getNodesByIds(doomedNodeIds);
        String doomedIdsStr = StringConverter.integersAsString(doomedNodeIds);

        try {
            LOG.debug(String.format("Removing nodes '[%s]' from load balancer '%d' in Zeus...", doomedIdsStr, msg.getLoadBalancerId()));
            dbLoadBalancer = nodeService.delNodes(dbLoadBalancer, doomedNodes);
            Set<Node> survivingNodes = nodeService.getAllNodesByAccountIdLoadBalancerId(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId());
            reverseProxyLoadBalancerService.setNodesPriorities(ZxtmNameBuilder.genVSName(dbLoadBalancer), dbLoadBalancer);
            reverseProxyLoadBalancerService.removeNodes(msg.getLoadBalancerId(), msg.getAccountId(), doomedNodes);
            LOG.debug(String.format("Successfully removed nodes '[%s]' from load balancer '%d' in Zeus.", doomedIdsStr, msg.getLoadBalancerId()));
        } catch (Exception e) {
            loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error removing nodes '%s' in Zeus for loadbalancer '%d'.", doomedIdsStr, msg.getLoadBalancerId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(msg.getAccountId(), msg.getLoadBalancerId(), e, ZEUS_FAILURE.name(), alertDescription);
            sendErrorToEventResource(msg, doomedNodeIds);
            return;
        }

        // Removes node from load balancer in DB


        // Refresh the LoadBalancer since the above may have been in a different transaction
        dbLoadBalancer = loadBalancerService.get(msg.getLoadBalancerId(), msg.getAccountId());

        // Update load balancer status in DB
        dbLoadBalancer.setStatus(LoadBalancerStatus.ACTIVE);
        loadBalancerService.update(dbLoadBalancer);

        //Set status record
        loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.ACTIVE);

        // Add atom entry
        String atomTitle = "Nodes Successfully Deleted";
        String atomSummary = "Nodes successfully deleted";
        for (Integer doomedNodeId : doomedNodeIds) {
            notificationService.saveNodeEvent(msg.getUserName(), msg.getAccountId(), msg.getLoadBalancerId(), doomedNodeId, atomTitle, atomSummary, DELETE_NODE, DELETE, INFO);
        }
        LOG.info(String.format("Delete node operation complete for load balancer '%d'.", msg.getLoadBalancerId()));
    }

    private void sendErrorToEventResource(MessageDataContainer msg) {
        String title = "Error Deleting Node";
        String desc = "Could not delete the node at this time.";
        notificationService.saveLoadBalancerEvent(msg.getUserName(), msg.getAccountId(), msg.getLoadBalancerId(), title, desc, DELETE_NODE, DELETE, CRITICAL);
    }

    private void sendErrorToEventResource(MessageDataContainer msg, List<Integer> nodeIds) {
        String title = "Error Deleting Nodes";
        String desc = "Could not delete the nodes at this time.";
        for (Integer nodeId : nodeIds) {
            notificationService.saveNodeEvent(msg.getUserName(), msg.getAccountId(), msg.getAccountId(), nodeId, title, desc, DELETE_NODE, DELETE, CRITICAL);
        }
    }
}
