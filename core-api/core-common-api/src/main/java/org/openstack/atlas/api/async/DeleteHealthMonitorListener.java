package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.HealthMonitorService;
import org.openstack.atlas.service.domain.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;

import static org.openstack.atlas.service.domain.common.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.common.AlertType.LBDEVICE_FAILURE;
import static org.openstack.atlas.service.domain.event.entity.CategoryType.DELETE;
import static org.openstack.atlas.service.domain.event.entity.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.event.entity.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.event.entity.EventType.DELETE_HEALTH_MONITOR;

@Component
public class DeleteHealthMonitorListener extends BaseListener {
    private final Log LOG = LogFactory.getLog(DeleteHealthMonitorListener.class);

    @Autowired
    private LoadBalancerRepository loadBalancerRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private HealthMonitorService healthMonitorService;

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
            LOG.debug(String.format("Removing health monitor for load balancer '%d' in LB Device...", dbLoadBalancer.getId()));
            reverseProxyLoadBalancerService.deleteHealthMonitor(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId());
            LOG.debug(String.format("Successfully removed health monitor for load balancer '%d' in LB Device.", dbLoadBalancer.getId()));
        } catch (Exception e) {
            loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error removing health monitor in LB Device for loadbalancer '%d'.", dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, LBDEVICE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);
            return;
        }

        try {
            LOG.debug(String.format("Removing health monitor for load balancer '%d' in database...", dbLoadBalancer.getId()));
            healthMonitorService.delete(dbLoadBalancer.getId());
            LOG.debug(String.format("Successfully removed health monitor for load balancer '%d' in database.", dbLoadBalancer.getId()));
        } catch (EntityNotFoundException e) {
            LOG.debug(String.format("Health monitor for load balancer #%d already deleted in database. Ignoring...", dbLoadBalancer.getId()));
        }

        loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ACTIVE);
        // Add atom entry
        String atomTitle = "Health Monitor Successfully Deleted";
        String atomSummary = "Health monitor successfully deleted";
        notificationService.saveHealthMonitorEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), dbLoadBalancer.getHealthMonitor().getId(), atomTitle, atomSummary, DELETE_HEALTH_MONITOR, DELETE, INFO);

        LOG.info(String.format("Delete health monitor operation complete for load balancer '%d'.", dbLoadBalancer.getId()));
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Deleting Health Monitor";
        String desc = "Could not delete the health monitor settings at this time.";
        notificationService.saveHealthMonitorEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), lb.getHealthMonitor().getId(), title, desc, DELETE_HEALTH_MONITOR, DELETE, CRITICAL);
    }
}
