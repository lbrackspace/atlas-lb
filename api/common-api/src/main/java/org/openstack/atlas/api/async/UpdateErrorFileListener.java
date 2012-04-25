package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.services.helpers.AlertType;

import javax.jms.Message;

import static org.openstack.atlas.service.domain.events.entities.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;

public class UpdateErrorFileListener extends BaseListener {
    private final Log LOG = LogFactory.getLog(UpdateErrorFileListener.class);

    @Override
    public void doOnMessage(Message message) throws Exception {
        String msg = String.format("Inside %s.doMessage", this.getClass().getName());
        LOG.info(msg);
        MessageDataContainer data = getDataContainerFromMessage(message);
        String content = data.getErrorFileContents();
        Integer aid = data.getAccountId();
        Integer lid = data.getLoadBalancerId();
        Integer clusterId = data.getClusterId();

        LoadBalancer dbLoadBalancer = null;


        try {
            LOG.debug("Grabbing loadbalancer...");
            dbLoadBalancer = loadBalancerService.get(data.getLoadBalancerId(), data.getAccountId());
            dbLoadBalancer.setUserName(data.getUserName());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", data.getLoadBalancerId());
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(data.getAccountId(), data.getLoadBalancerId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(dbLoadBalancer);
            loadBalancerStatusHistoryService.save(data.getAccountId(), data.getLoadBalancerId(), LoadBalancerStatus.ERROR);

            return;
        }

        if (aid != null && lid != null) {
            try {
                LOG.debug("Attempting to set error file in zeus...calling setErrorFile");
                reverseProxyLoadBalancerService.setErrorFile(dbLoadBalancer, content);
                LOG.debug("Successfully updated error file in zeus.");
            } catch (Exception e) {
                loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);

                String tmpMsg = String.format("Error setting Errorfile for %d_%d", aid, lid);
                LOG.error(tmpMsg, e);
                notificationService.saveAlert(aid, lid, e, AlertType.ZEUS_FAILURE.name(), msg);
                sendErrorToEventResource(dbLoadBalancer);

            }
        } else if (clusterId != null) {
            LOG.debug("Attempting to upload default error file in zeus...calling uploadDefaultErrorFile");
            try {
                reverseProxyLoadBalancerService.uploadDefaultErrorFile(clusterId, content);
                LOG.debug("Successfully uploaded default error file in zeus.");
            } catch (Exception e) {
                String tmpMsg = String.format("Error uploading default error file...");
                LOG.error(tmpMsg, e);
                notificationService.saveAlert(null, null, e, AlertType.ZEUS_FAILURE.name(), msg);
            }
        }

        loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ACTIVE);
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Updating Load Balancer Error page...";
        String desc = "Could not update a load balancer Error page at this time.";
        notificationService.saveLoadBalancerEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, EventType.UPDATE_LOADBALANCER, UPDATE, CRITICAL);
    }
}
