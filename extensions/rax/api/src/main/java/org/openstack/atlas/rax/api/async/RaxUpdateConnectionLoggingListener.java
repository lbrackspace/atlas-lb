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

import static org.openstack.atlas.api.atom.EntryHelper.UPDATE_LOGGING_TITLE;
import static org.openstack.atlas.service.domain.event.entity.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.event.entity.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.event.entity.EventType.UPDATE_CONNECTION_LOGGING;

@Component
public class RaxUpdateConnectionLoggingListener extends BaseListener {

    final Log LOG = LogFactory.getLog(RaxUpdateConnectionLoggingListener.class);

    @Autowired
    private LoadBalancerRepository loadBalancerRepository;
    @Autowired
    private NotificationService notificationService;
    @Override

    public void doOnMessage(Message message) throws Exception {
        MessageDataContainer dataContainer = getDataContainerFromMessage(message);
        RaxLoadBalancer loadBalancer = (RaxLoadBalancer) dataContainer.getLoadBalancer();
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

        try {
            LOG.debug(String.format("Updating connection logging for load balancer '%d' in LB Device...", dbLoadBalancer.getId()));
            ((RaxProxyService)reverseProxyLoadBalancerService).updateConnectionLogging(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), dbLoadBalancer.getConnectionLogging(), dbLoadBalancer.getProtocol());
            LOG.debug(String.format("Successfully updated connection logging for load balancer '%d' in LB Device.", dbLoadBalancer.getId()));
        } catch (Exception e) {
            loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error updating connection logging for load balancer '%d' in LB Device.", dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, AlertType.LBDEVICE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(loadBalancer);
            return;
        }

        String desc = "Connection logging successully set to " + dbLoadBalancer.getConnectionLogging().toString();
        notificationService.saveLoadBalancerEvent(loadBalancer.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), UPDATE_LOGGING_TITLE, desc, UPDATE_CONNECTION_LOGGING, UPDATE, EventSeverity.INFO);

        // Update load balancer status in DB
        loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ACTIVE);

        LOG.info("Update connection logging operation complete.");
    }

    private void sendErrorToEventResource(RaxLoadBalancer lb) {
        String title = "Error Updating Connection Logging";
        String desc = "Could not update connection logging at this time.";
        notificationService.saveLoadBalancerEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, UPDATE_CONNECTION_LOGGING, UPDATE, CRITICAL);
    }
}
