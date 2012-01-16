package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.helper.NodesHelper;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.Node;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.LoadBalancerService;
import org.openstack.atlas.service.domain.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.openstack.atlas.datamodel.CoreLoadBalancerStatus.ACTIVE;
import static org.openstack.atlas.datamodel.CoreLoadBalancerStatus.ERROR;
import static org.openstack.atlas.datamodel.CoreNodeStatus.OFFLINE;
import static org.openstack.atlas.service.domain.service.helpers.AlertType.*;
import static org.openstack.atlas.service.domain.event.entity.EventType.*;
import static org.openstack.atlas.service.domain.event.entity.CategoryType.*;
import static org.openstack.atlas.service.domain.event.entity.EventSeverity.*;

import javax.jms.Message;

@Component
public class UpdateNodeListener extends BaseListener {
    private final Log LOG = LogFactory.getLog(UpdateNodeListener.class);

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private LoadBalancerRepository loadBalancerRepository;

    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);
        LoadBalancer dbLoadBalancer;

        MessageDataContainer dataContainer = getDataContainerFromMessage(message);
        LoadBalancer queueLb = dataContainer.getLoadBalancer();

        try {
            dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(queueLb.getId(), queueLb.getAccountId());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", queueLb.getId());
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(queueLb.getAccountId(), queueLb.getId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);
            return;
        }

        Node nodeToUpdate = getNodeToUpdate(queueLb);

        try {
            LOG.info(String.format("Updating nodes for load balancer '%d' in LB Device...", dbLoadBalancer.getId()));
            reverseProxyLoadBalancerService.updateNode(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), nodeToUpdate);
            LOG.info(String.format("Successfully updated nodes for load balancer '%d' in LB Device.", dbLoadBalancer.getId()));
        } catch (Exception e) {
            dbLoadBalancer.setStatus(ERROR);
            NodesHelper.setNodesToStatus(dbLoadBalancer, OFFLINE);
            loadBalancerRepository.update(dbLoadBalancer);
            String alertDescription = String.format("Error updating node '%d' in LB Device for loadbalancer '%d'.", nodeToUpdate.getId(), dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, LBDEVICE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb, nodeToUpdate);
            return;
        }

        // Update load balancer status in DB
        dbLoadBalancer.setStatus(ACTIVE);
        loadBalancerRepository.update(dbLoadBalancer);

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

    private StringBuffer createAtomSummary(Node node) {
        StringBuffer atomSummary = new StringBuffer();
        atomSummary.append("Node successfully updated with ");
        atomSummary.append("address: '").append(node.getAddress()).append("', ");
        atomSummary.append("port: '").append(node.getPort()).append("', ");
        atomSummary.append("weight: '").append(node.getWeight()).append("', ");
        atomSummary.append("condition: '").append(node.isEnabled()).append("'");
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
