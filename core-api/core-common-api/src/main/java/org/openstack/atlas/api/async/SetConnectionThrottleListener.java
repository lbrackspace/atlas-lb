package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.atom.EntryHelper;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;

import static org.openstack.atlas.api.atom.EntryHelper.UPDATE_THROTTLE_TITLE;
import static org.openstack.atlas.api.helper.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.api.helper.AlertType.LBDEVICE_FAILURE;
import static org.openstack.atlas.service.domain.event.entity.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.event.entity.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.event.entity.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.event.entity.EventType.SET_CONNECTION_THROTTLE;

@Component
public class SetConnectionThrottleListener extends BaseListener {
    private final Log LOG = LogFactory.getLog(SetConnectionThrottleListener.class);

    @Autowired
    private LoadBalancerRepository loadBalancerRepository;
    @Autowired
    private NotificationService notificationService;

    @Override
    public void doOnMessage(final Message message) throws Exception {
        MessageDataContainer dataContainer = getDataContainerFromMessage(message);
        LoadBalancer queueLb = dataContainer.getLoadBalancer();
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
            LOG.debug(String.format("Updating connection throttle for load balancer '%d' in LB Device...", dbLoadBalancer.getId()));
            reverseProxyLoadBalancerService.updateConnectionThrottle(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), dbLoadBalancer.getConnectionThrottle());
            LOG.debug(String.format("Successfully updated connection throttle for load balancer '%d' in LB Device...", dbLoadBalancer.getId()));
        } catch (Exception e) {
            loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error updating connection throttle in LB Device for loadbalancer '%d'.", dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, LBDEVICE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);
            return;
        }

        // Update load balancer status in DB
        loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ACTIVE);

        // Add atom entry
        notificationService.saveConnectionThrottleEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), dbLoadBalancer.getConnectionThrottle().getId(), UPDATE_THROTTLE_TITLE, EntryHelper.createConnectionThrottleSummary(dbLoadBalancer), SET_CONNECTION_THROTTLE, UPDATE, INFO);

        LOG.info(String.format("Update connection throttle operation complete for load balancer '%d'.", dbLoadBalancer.getId()));
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Updating Connection Throttle";
        String desc = "Could not update the connection throttle settings at this time.";
        notificationService.saveConnectionThrottleEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), lb.getConnectionThrottle().getId(), title, desc, SET_CONNECTION_THROTTLE, UPDATE, CRITICAL);
    }
}
