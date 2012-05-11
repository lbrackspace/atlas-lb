package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeStatus;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.ZeusEvent;
import org.openstack.atlas.service.domain.services.CallbackService;
import org.openstack.atlas.service.domain.services.NodeService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import static org.openstack.atlas.service.domain.events.entities.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.UPDATE_NODE;
import static org.openstack.atlas.service.domain.services.helpers.CallbackHelper.NODE_FAIL_TAG;
import static org.openstack.atlas.service.domain.services.helpers.CallbackHelper.NODE_WORKING_TAG;

public class CallbackServiceImpl extends BaseService implements CallbackService {
    private final Log LOG = LogFactory.getLog(CallbackServiceImpl.class);
    private NodeService nodeService;
    private NotificationService notificationService;

    @Required
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Required
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public void handleZeusEvent(ZeusEvent zeusEvent) throws BadRequestException {
        // Example ipv4 paramLine: "INFO pools/501148_11066 nodes/10.179.78.70:80 nodeworking Node 10.179.78.70 is working again"
        // Example ipv6 paramLine: "INFO pools/501148_11066 nodes/[fe80::4240:adff:fe5c:c9ee]:80 nodeworking Node fe80::4240:adff:fe5c:c9ee is working again"

        if (zeusEvent.getParamLine().contains(NODE_FAIL_TAG) || zeusEvent.getParamLine().contains(NODE_WORKING_TAG)) {
            LOG.debug("Node status changed.");
        } else {
            LOG.warn("Unsupported callback event triggered. Dropping request...");
            throw new BadRequestException("We currently do not support this callback request.");
        }

        try {
            Integer loadBalancerId = getLoadbalancerId(zeusEvent.getParamLine());
            String ipAddress = getIpAddress(zeusEvent.getParamLine());
            Integer ipPort = getIpPort(zeusEvent.getParamLine());
            Node dbNode = nodeService.getNodeByLoadBalancerIdIpAddressAndPort(loadBalancerId, ipAddress, ipPort);
            String status;

            if (zeusEvent.getParamLine().contains(NODE_FAIL_TAG)) {
                dbNode.setStatus(NodeStatus.OFFLINE);
                status = NodeStatus.OFFLINE.name();
            } else if (zeusEvent.getParamLine().contains(NODE_WORKING_TAG)) {
                dbNode.setStatus(NodeStatus.ONLINE);
                status = NodeStatus.ONLINE.name();
            } else {
                throw new BadRequestException("We currently do not support this callback request.");
            }

            nodeService.updateNodeStatus(dbNode);

            // Add atom entry
            String atomTitle = "Node Status Updated";
            String atomSummary = String.format("Node '%d' status changed to '%s' for load balancer '%d'", dbNode.getId(), status, loadBalancerId);
            notificationService.saveNodeEvent("Rackspace Cloud", dbNode.getLoadbalancer().getAccountId(), loadBalancerId, dbNode.getId(), atomTitle, atomSummary, UPDATE_NODE, UPDATE, INFO);

            LOG.info(String.format("Node '%d' status changed to '%s' for load balancer '%d'", dbNode.getId(), status, loadBalancerId));
        } catch (Exception e) {
            String message;
            if (e instanceof EntityNotFoundException) {
                message = String.format("Could not process Zeus event as node could not be found: '%s'", zeusEvent.getParamLine());
            } else {
                message = String.format("Could not process Zeus event: '%s'", zeusEvent.getParamLine());
            }
            LOG.warn(message);
            throw new BadRequestException(message, e);
        }
    }

    public Integer getLoadbalancerId(String paramLine) throws Exception {
        String poolsObject = paramLine.split(" ")[1];
        String poolName = poolsObject.split("/")[1];
        String loadbalancerId = poolName.split("_")[1];

        try {
            return Integer.parseInt(loadbalancerId);
        } catch (NumberFormatException e) {
            LOG.warn(String.format("Error converting string to integer for load balancer id: '%s'", loadbalancerId));
            throw new Exception(e);
        }
    }

    public String getIpAddress(String paramLine) {
//        String nodesObject = paramLine.split(" ")[2];
//        String ipAddressWithPort = nodesObject.split("/")[1];
//        return ipAddressWithPort.split(":")[0].replace("[", "");
        String nodeLine = paramLine.split("Node ")[1];
        return nodeLine.split(" ")[0];
    }

    public Integer getIpPort(String paramLine) throws Exception {
        String nodesObject = paramLine.split(" ")[2];
        String ipAddressWithPort = nodesObject.split("/")[1];
        String port = ipAddressWithPort.split(":")[1];

        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            LOG.info("Error parsing paramline for ipv4, trying for ipv6");
        }
        return getIpPortForIpv6(paramLine);
    }

    public Integer getIpPortForIpv6(String paramLine) throws Exception {
        String nodesObject = paramLine.split(" ")[2];
        String ipAddressWithPort = nodesObject.split("/")[1];
        String port = ipAddressWithPort.split("]:")[1];

        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            LOG.warn(String.format("Error converting string to integer for port: '%s'", port));
            throw new Exception(e);
        }
    }
}
