package org.openstack.atlas.api.async;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeStatus;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.helpers.NodesHelper;
import org.openstack.atlas.api.atom.EntryHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Message;

import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.LBDEVICE_FAILURE;
import static org.openstack.atlas.service.domain.events.entities.CategoryType.CREATE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.CREATE_NODE;
import static org.openstack.atlas.api.atom.EntryHelper.CREATE_NODE_TITLE;

public class CreateNodesListener extends BaseListener {

    private final Log LOG = LogFactory.getLog(CreateNodesListener.class);

    @Override
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
        
        try {
            LOG.debug("Setting nodes in LBDevice...");
            reverseProxyLoadBalancerService.setNodes(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getNodes());
            LOG.debug("Nodes successfully set.");
        } catch (Exception e) {
            loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
            String alertDescription = "Error setting nodes in LB Device for loadbalancer #" + dbLoadBalancer.getId();
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, LBDEVICE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);
            return;
        }

        // Update load balancer in DB
        dbLoadBalancer.setStatus(LoadBalancerStatus.ACTIVE);
        NodesHelper.setNodesToStatus(queueLb, dbLoadBalancer, NodeStatus.ONLINE);
        loadBalancerService.update(dbLoadBalancer);

        // Add atom entries for new nodes only
        for (Node dbNode : dbLoadBalancer.getNodes()) {
            for (Node queueNode : queueLb.getNodes()) {
                if (queueNode.getIpAddress().equals(dbNode.getIpAddress()) && queueNode.getPort().equals(dbNode.getPort()))
                    notificationService.saveNodeEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(),
                            dbNode.getId(), CREATE_NODE_TITLE, EntryHelper.createNodeSummary(dbNode), CREATE_NODE, CREATE, INFO);
            }
        }

        LOG.info(String.format("Create nodes operation successfully completed for load balancer '%d'", dbLoadBalancer.getId()));
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Creating Node";
        String desc = "Could not create the node at this time";
        for (Node node : lb.getNodes()) {
            notificationService.saveNodeEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), node.getId(), title, desc, CREATE_NODE, CREATE, CRITICAL);
        }
    }
}
