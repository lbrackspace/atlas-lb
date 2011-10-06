package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.ConnectionThrottleService;
import org.openstack.atlas.service.domain.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;

import static org.openstack.atlas.service.domain.common.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.common.AlertType.LBDEVICE_FAILURE;
import static org.openstack.atlas.service.domain.event.entity.CategoryType.DELETE;
import static org.openstack.atlas.service.domain.event.entity.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.event.entity.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.event.entity.EventType.DELETE_CONNECTION_THROTTLE;

@Component
public class DeleteConnectionThrottleListener extends BaseListener {
    private final Log LOG = LogFactory.getLog(DeleteConnectionThrottleListener.class);

    @Autowired
    private LoadBalancerRepository loadBalancerRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private ConnectionThrottleService connectionThrottleService;

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LoadBalancer queueLb = getDataContainerFromMessage(message).getLoadBalancer();
        LoadBalancer dbLoadBalancer;

        try {
            dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(queueLb.getId(), queueLb.getAccountId());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", queueLb.getId());
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(queueLb.getAccountId(), queueLb.getId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);
            return;
        }

        try {
            LOG.debug(String.format("Removing connection throttle for load balancer '%d' in LB Device...", dbLoadBalancer.getId()));
            reverseProxyLoadBalancerService.deleteConnectionThrottle(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId());
            LOG.debug(String.format("Successfully removed connection throttle for load balancer '%d' in LB Device.", dbLoadBalancer.getId()));
        } catch (Exception e) {
            loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error removing connection throttle in LB Device for loadbalancer '%d'.", dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, LBDEVICE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);
            return;
        }

        try {
            LOG.debug(String.format("Removing connection throttle for load balancer '%d' in database...", dbLoadBalancer.getId()));
            connectionThrottleService.delete(dbLoadBalancer.getId());
            LOG.debug(String.format("Successfully removed connection throttle for load balancer '%d' in database.", dbLoadBalancer.getId()));
        } catch (EntityNotFoundException e) {
            LOG.debug(String.format("Connection throttle for load balancer #%d already deleted in database. Ignoring...", dbLoadBalancer.getId()));
        }

        loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ACTIVE);
        // Add atom entry
        String atomTitle = "Connection Throttle Successfully Deleted";
        String atomSummary = "Connection throttle successfully deleted";
        notificationService.saveConnectionThrottleEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), dbLoadBalancer.getConnectionThrottle().getId(), atomTitle, atomSummary, DELETE_CONNECTION_THROTTLE, DELETE, INFO);

        LOG.info(String.format("Delete connection throttle operation complete for load balancer '%d'.", dbLoadBalancer.getId()));
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Deleting Connection Throttle";
        String desc = "Could not delete the connection throttle settings at this time.";
        notificationService.saveConnectionThrottleEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), lb.getConnectionThrottle().getId(), title, desc, DELETE_CONNECTION_THROTTLE, DELETE, CRITICAL);
    }
}
