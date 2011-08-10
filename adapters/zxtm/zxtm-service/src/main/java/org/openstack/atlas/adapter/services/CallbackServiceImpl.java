package org.openstack.atlas.adapter.services;

import org.openstack.atlas.service.domain.services.impl.BaseService;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeStatus;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.pojos.LBDeviceEvent;
import org.openstack.atlas.service.domain.services.CallbackService;
import org.openstack.atlas.service.domain.services.NodeService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;


import static org.openstack.atlas.service.domain.events.entities.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.UPDATE_NODE;
import static org.openstack.atlas.adapter.services.helpers.CallbackHelper.NODE_FAIL_TAG;
import static org.openstack.atlas.adapter.services.helpers.CallbackHelper.NODE_WORKING_TAG;

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
    public void handleLBDeviceEvent(LBDeviceEvent lbDeviceEvent) throws BadRequestException {
        // Example paramLine: "INFO pools/501148_11066 nodes/10.179.78.70:80 nodeworking Node 10.179.78.70 is working again"

        if (lbDeviceEvent.getParamLine().contains(NODE_FAIL_TAG) || lbDeviceEvent.getParamLine().contains(NODE_WORKING_TAG)) {
            LOG.debug("Node status changed.");
        } else {
            LOG.warn("Unsupported callback event triggered. Dropping request...");
            throw new BadRequestException("We currently do not support this callback request.");
        }

        Integer loadBalancerId = getLoadbalancerId(lbDeviceEvent.getParamLine());
        String ipAddress = getIpAddress(lbDeviceEvent.getParamLine());
        Integer ipPort = getIpPort(lbDeviceEvent.getParamLine());
        Node dbNode = nodeService.getNodeByLoadBalancerIdIpAddressAndPort(loadBalancerId, ipAddress, ipPort);
        String status;

        if (lbDeviceEvent.getParamLine().contains(NODE_FAIL_TAG)) {
            dbNode.setStatus(NodeStatus.OFFLINE);
            status = NodeStatus.OFFLINE.name();
        } else if (lbDeviceEvent.getParamLine().contains(NODE_WORKING_TAG)) {
            dbNode.setStatus(NodeStatus.ONLINE);
            status = NodeStatus.ONLINE.name();
        } else {
            throw new BadRequestException("We currently do not support this callback request.");
        }

        nodeService.updateNodeStatus(dbNode);

        // Add atom entry
        String atomTitle = "Node Status Updated";
        String atomSummary = String.format("Node '%d' status changed to '%s' for load balancer '%d'", dbNode.getId(), status, loadBalancerId);
        notificationService.saveNodeEvent("OpenStack Cloud", dbNode.getLoadbalancer().getAccountId(), loadBalancerId, dbNode.getId(), atomTitle, atomSummary, UPDATE_NODE, UPDATE, INFO);

        LOG.info(String.format("Node '%d' status changed to '%s' for load balancer '%d'", dbNode.getId(), status, loadBalancerId));
    }

    public static Integer getLoadbalancerId(String paramLine) {
        String poolsObject = paramLine.split(" ")[1];
        String poolName = poolsObject.split("/")[1];
        String loadbalancerId = poolName.split("_")[1];
        return Integer.parseInt(loadbalancerId);
    }

    public static String getIpAddress(String paramLine) {
        String nodesObject = paramLine.split(" ")[2];
        String ipAddressWithPort = nodesObject.split("/")[1];
        String ipAddress = ipAddressWithPort.split(":")[0];
        return ipAddress;
    }

    public static Integer getIpPort(String paramLine) {
        String nodesObject = paramLine.split(" ")[2];
        String ipAddressWithPort = nodesObject.split("/")[1];
        String port = ipAddressWithPort.split(":")[1];
        return Integer.parseInt(port);
    }
}
