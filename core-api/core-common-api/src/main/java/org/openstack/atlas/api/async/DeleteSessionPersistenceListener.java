package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.NotificationService;
import org.openstack.atlas.service.domain.service.SessionPersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;

import static org.openstack.atlas.service.domain.common.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.common.AlertType.LBDEVICE_FAILURE;
import static org.openstack.atlas.service.domain.event.entity.CategoryType.DELETE;
import static org.openstack.atlas.service.domain.event.entity.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.event.entity.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.event.entity.EventType.DELETE_SESSION_PERSISTENCE;

@Component
public class DeleteSessionPersistenceListener extends BaseListener {
    private final Log LOG = LogFactory.getLog(DeleteSessionPersistenceListener.class);

    @Autowired
    private LoadBalancerRepository loadBalancerRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private SessionPersistenceService sessionPersistenceService;

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
            LOG.debug(String.format("Removing session persistence for load balancer '%d' in LB Device...", dbLoadBalancer.getId()));
            reverseProxyLoadBalancerService.deleteSessionPersistence(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId());
            LOG.debug(String.format("Successfully removed session persistence for load balancer '%d' in LB Device.", dbLoadBalancer.getId()));
        } catch (Exception e) {
            loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error removing session persistence in LB Device for loadbalancer '%d'.", dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, LBDEVICE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);
            return;
        }

        try {
            LOG.debug(String.format("Removing session persistence for load balancer '%d' in database...", dbLoadBalancer.getId()));
            sessionPersistenceService.delete(dbLoadBalancer.getId());
            LOG.debug(String.format("Successfully removed session persistence for load balancer '%d' in database.", dbLoadBalancer.getId()));
        } catch (EntityNotFoundException e) {
            LOG.debug(String.format("Session persistence for load balancer #%d already deleted in database. Ignoring...", dbLoadBalancer.getId()));
        }

        loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ACTIVE);
        // Add atom entry
        String atomTitle = "Session Persistence Successfully Deleted";
        String atomSummary = "Session persistence successfully deleted";
        notificationService.saveSessionPersistenceEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), atomTitle, atomSummary, DELETE_SESSION_PERSISTENCE, DELETE, INFO);

        LOG.info(String.format("Delete session persistence operation complete for load balancer '%d'.", dbLoadBalancer.getId()));
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Deleting Session Persistence";
        String desc = "Could not delete the session persistence settings at this time.";
        notificationService.saveSessionPersistenceEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, DELETE_SESSION_PERSISTENCE, DELETE, CRITICAL);
    }
}
