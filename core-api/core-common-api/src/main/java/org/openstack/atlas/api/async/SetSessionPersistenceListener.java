package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;

import static org.openstack.atlas.api.atom.EntryHelper.UPDATE_PERSISTENCE_TITLE;
import static org.openstack.atlas.api.helper.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.api.helper.AlertType.LBDEVICE_FAILURE;
import static org.openstack.atlas.service.domain.event.entity.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.event.entity.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.event.entity.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.event.entity.EventType.SET_SESSION_PERSISTENCE;

@Component
public class SetSessionPersistenceListener extends BaseListener {
    private final Log LOG = LogFactory.getLog(SetSessionPersistenceListener.class);

    @Autowired
    private LoadBalancerRepository loadBalancerRepository;
    @Autowired
    private NotificationService notificationService;

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);
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
            LOG.debug(String.format("Updating session persistence for load balancer '%d' in LB Device...", dbLoadBalancer.getId()));
            reverseProxyLoadBalancerService.setSessionPersistence(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getSessionPersistence());
            LOG.debug(String.format("Successfully updated session persistence for load balancer '%d' in LB Device...", dbLoadBalancer.getId()));
        } catch (Exception e) {
            loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error updating session persistence in LB Device for loadbalancer '%d'.", dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, LBDEVICE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);
            return;
        }

        // Update load balancer status in DB
        loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ACTIVE);
        // Add atom entry
        String atomSummary = String.format("Session persistence successfully updated to '%s'", dbLoadBalancer.getSessionPersistence().getPersistenceType());
        notificationService.saveSessionPersistenceEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), UPDATE_PERSISTENCE_TITLE, atomSummary, SET_SESSION_PERSISTENCE, UPDATE, INFO);

        LOG.info(String.format("Update session persistence operation complete for load balancer '%d'.", dbLoadBalancer.getId()));
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Updating Session Persistence";
        String desc = "Could not update the session persistence settings at this time.";
        notificationService.saveSessionPersistenceEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, SET_SESSION_PERSISTENCE, UPDATE, CRITICAL);
    }
}
