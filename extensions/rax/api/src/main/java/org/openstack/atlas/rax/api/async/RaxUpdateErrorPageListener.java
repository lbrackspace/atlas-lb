package org.openstack.atlas.rax.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.async.BaseListener;
import org.openstack.atlas.api.helper.AlertType;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.rax.api.integration.RaxProxyService;
import org.openstack.atlas.rax.domain.entity.RaxLoadBalancer;
import org.openstack.atlas.service.domain.event.entity.EventSeverity;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;

import static org.openstack.atlas.service.domain.event.entity.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.event.entity.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.event.entity.EventType.UPDATE_ERROR_PAGE;


@Component
public class RaxUpdateErrorPageListener extends BaseListener {

    final Log LOG = LogFactory.getLog(RaxUpdateErrorPageListener.class);

    @Autowired
    private LoadBalancerRepository loadBalancerRepository;
    @Autowired
    private NotificationService notificationService;
    @Override

    public void doOnMessage(Message message) throws Exception {
        MessageDataContainer dataContainer = getDataContainerFromMessage(message);
        RaxLoadBalancer loadBalancer = (RaxLoadBalancer) dataContainer.getLoadBalancer();
        String content = (String) dataContainer.getResource();
        RaxLoadBalancer dbLoadBalancer;

        try {
            dbLoadBalancer = (RaxLoadBalancer)loadBalancerRepository.getByIdAndAccountId(loadBalancer.getId(), loadBalancer.getAccountId());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", loadBalancer.getId());
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(loadBalancer.getAccountId(), loadBalancer.getId(), enfe, AlertType.DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(loadBalancer);
            return;
        }

        if(dataContainer.getAccountId() != null && dataContainer.getLoadBalancerId() != null) {
            try {
                LOG.debug("Attempting to set error file in zeus...calling setErrorFile");
                ((RaxProxyService)reverseProxyLoadBalancerService).setErrorPage(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), content);
                LOG.debug("Successfully updated error file in zeus.");
            } catch (Exception e) {
                loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ERROR);
                String alertDescription = String.format("Error setting Errorfile for load balancer '%d' in LB Device.", dbLoadBalancer.getId());
                LOG.error(alertDescription, e);
                notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, AlertType.LBDEVICE_FAILURE.name(), alertDescription);
                sendErrorToEventResource(loadBalancer);
                return;
            }
        } else  { //if (clusterId != null)
            Integer clusterId = null;
            LOG.debug("Attempting to upload default error file in zeus...calling uploadDefaultErrorFile");
            try {
                ((RaxProxyService)reverseProxyLoadBalancerService).uploadDefaultErrorPage(clusterId, content);
                LOG.debug("Successfully uploaded default error file in zeus.");
            } catch (Exception e) {
                String tmpMsg = String.format("Error uploading default error file...");
                LOG.error(tmpMsg, e);
                notificationService.saveAlert(null, null, e, AlertType.LBDEVICE_FAILURE.name(), tmpMsg);
            }
        }

        String desc = "Error Page successully set for loadbalancer " + dbLoadBalancer.getId();
        notificationService.saveLoadBalancerEvent(loadBalancer.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), desc, desc, UPDATE_ERROR_PAGE, UPDATE, EventSeverity.INFO);

        // Update load balancer status in DB
        loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ACTIVE);

        LOG.info("Update error page operation complete.");
    }

    private void sendErrorToEventResource(RaxLoadBalancer lb) {
        String title = "Error setting Error File";
        String desc = "Could not set Error Page at this time.";
        notificationService.saveLoadBalancerEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, UPDATE_ERROR_PAGE, UPDATE, CRITICAL);
    }
}
