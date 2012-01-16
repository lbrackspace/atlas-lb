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

import static org.openstack.atlas.service.domain.event.entity.CategoryType.DELETE;
import static org.openstack.atlas.service.domain.event.entity.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.event.entity.EventType.*;

@Component
public class RaxDeleteErrorPageListener extends BaseListener {

    final Log LOG = LogFactory.getLog(RaxDeleteErrorPageListener.class);

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
                LOG.debug("Attempting to delete error file in zeus...calling setErrorFile");
                ((RaxProxyService)reverseProxyLoadBalancerService).deleteErrorPage(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId());
                LOG.debug("Successfully deleted error file in zeus.");
            } catch (Exception e) {
                loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ERROR);
                String alertDescription = String.format("Error deleting Errorfile for load balancer '%d' in LB Device.", dbLoadBalancer.getId());
                LOG.error(alertDescription, e);
                notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, AlertType.LBDEVICE_FAILURE.name(), alertDescription);
                sendErrorToEventResource(loadBalancer);
                return;
            }
        }
        String desc = "Error Page successully deleted for loadbalancer " + dbLoadBalancer.getId();
        notificationService.saveLoadBalancerEvent(loadBalancer.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), desc, desc, DELETE_ERROR_PAGE, DELETE, EventSeverity.INFO);

        // Update load balancer status in DB
        loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ACTIVE);

        LOG.info("Delete error page operation complete.");
    }

    private void sendErrorToEventResource(RaxLoadBalancer lb) {
        String title = "Error setting Error File";
        String desc = "Could not set Error Page at this time.";
        notificationService.saveLoadBalancerEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, DELETE_ERROR_PAGE, DELETE, CRITICAL);
    }
}
