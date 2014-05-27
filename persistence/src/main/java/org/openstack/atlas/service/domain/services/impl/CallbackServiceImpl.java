package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeStatus;
import org.openstack.atlas.service.domain.events.entities.Alert;
import org.openstack.atlas.service.domain.events.entities.AlertStatus;
import org.openstack.atlas.service.domain.events.repository.AlertRepository;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.pojos.ZeusEvent;
import org.openstack.atlas.service.domain.services.*;
import org.openstack.atlas.service.domain.services.helpers.CallbackHelper;
import org.openstack.atlas.service.domain.services.helpers.StringHelper;
import org.openstack.atlas.service.domain.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.ACTIVE;
import static org.openstack.atlas.service.domain.entities.NodeStatus.ONLINE;
import static org.openstack.atlas.service.domain.events.entities.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.UPDATE_NODE;
import static org.openstack.atlas.service.domain.services.helpers.CallbackHelper.*;

@Service
public class CallbackServiceImpl extends BaseService implements CallbackService {
    private final Log LOG = LogFactory.getLog(CallbackServiceImpl.class);

    @Autowired
    private NodeService nodeService;
    @Autowired
    private NotificationService notificationService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    //V1-B-34716
    @Override
    @Transactional
    public void handleZeusEvent(ZeusEvent zeusEvent) throws BadRequestException {
        //Example paramLines::
        //WARN monitors/582112_23321 monitorfail Monitor has detected a failure in node 'f6fde0b59be015ac.rackspaceclouddb.com:80 (10.1.1.48)': Write failed: Connection refused
        //WARN monitors/571432_62203 monitorfail Monitor has detected a failure in node '10.178.224.134:443': Invalid HTTP response received; premature end of headers
        //INFO monitors/571432_62203 monitorok Monitor is working for node '10.178.224.134:443'.


        if (zeusEvent.getParamLine().contains(MONITOR_FAIL_TAG) || zeusEvent.getParamLine().contains(MONITOR_WORKING_TAG)) {
            LOG.debug("Node status changed.");
        } else {
            LOG.warn("Unsupported callback event triggered. Dropping request...");
            throw new BadRequestException("We currently do not support this callback request.");
        }

        try {
            CallbackHelper callbackHelper = new CallbackHelper(zeusEvent.getParamLine());
            Integer loadBalancerId = callbackHelper.loadBalancerId;
            String ipAddress = callbackHelper.ipAddress;
            Integer ipPort = callbackHelper.port;
            Node dbNode = nodeService.getNodeByLoadBalancerIdIpAddressAndPort(loadBalancerId, ipAddress, ipPort);
            String status;

            if (zeusEvent.getParamLine().contains(MONITOR_FAIL_TAG)) {
                dbNode.setStatus(NodeStatus.OFFLINE);
                status = NodeStatus.OFFLINE.name();
            } else if (zeusEvent.getParamLine().contains(MONITOR_WORKING_TAG)) {
                dbNode.setStatus(NodeStatus.ONLINE);
                status = NodeStatus.ONLINE.name();
            } else {
                throw new BadRequestException("We currently do not support this callback request.");
            }

            nodeService.updateNodeStatus(dbNode);

            String atomTitle = "Node Status Updated";
            String atomSummary = String.format("Node '%d' status changed to '%s' for load balancer '%d'", dbNode.getId(), status, loadBalancerId);
            String detailedMessage = (callbackHelper.detailedMessage.equals("")) ? "Node is working" : callbackHelper.detailedMessage;
            notificationService.saveNodeServiceEvent("Rackspace Cloud", dbNode.getLoadbalancer().getAccountId(), loadBalancerId, dbNode.getId(), atomTitle, atomSummary, UPDATE_NODE, UPDATE, INFO, detailedMessage);

            LOG.info(String.format("Node '%d' status changed to '%s' for load balancer '%d'", dbNode.getId(), status, loadBalancerId));
        } catch (Exception e) {
            String message;
            if (e instanceof EntityNotFoundException) {
                message = String.format("Could not process Zeus event as node could not be found: '%s'", zeusEvent.getParamLine());
            } else {
                message = String.format("Could not process Zeus event: '%s'", zeusEvent.getParamLine());
            }
            throw new BadRequestException(message, e);
        }
    }
}
