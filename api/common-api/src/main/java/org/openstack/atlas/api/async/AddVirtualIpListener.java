package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.atom.EntryHelper;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.UsageEventCollectionException;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.util.debug.Debug;

import javax.jms.Message;

import java.util.Calendar;

import static org.openstack.atlas.api.atom.EntryHelper.CREATE_VIP_TITLE;
import static org.openstack.atlas.service.domain.events.entities.CategoryType.CREATE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.USAGE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.ZEUS_FAILURE;

public class AddVirtualIpListener extends BaseListener {

    private final Log LOG = LogFactory.getLog(AddVirtualIpListener.class);

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);

        MessageDataContainer dataContainer = getDataContainerFromMessage(message);
        LoadBalancer dbLoadBalancer;

        try {
            dbLoadBalancer = loadBalancerService.getWithUserPages(dataContainer.getLoadBalancerId());
            if (dataContainer.getAccountId() == null) dataContainer.setAccountId(dbLoadBalancer.getAccountId());
            if (dataContainer.getUserName() == null) dataContainer.setUserName(dbLoadBalancer.getUserName());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", dataContainer.getLoadBalancerId());
            LOG.error(alertDescription, enfe);
            if (dataContainer.getUserName() == null) dataContainer.setUserName("");
            if (dataContainer.getAccountId() == null) dataContainer.setAccountId(0);
            notificationService.saveAlert(dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(dataContainer);
            return;
        }

        try {
            if (isRestAdapter()) {
                LOG.debug(String.format("Adding Virtual ip to load balancer '%d' in STM...", dbLoadBalancer.getId()));
                reverseProxyLoadBalancerVTMService.addVirtualIps(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId(), dbLoadBalancer);
                LOG.debug("Successfully added virtual ip in Zeus.");
            } else {
                LOG.debug(String.format("Adding Virtual ip to load balancer '%d' in ZXTM...", dbLoadBalancer.getId()));
                reverseProxyLoadBalancerService.addVirtualIps(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId(), dbLoadBalancer);
                LOG.debug("Successfully added virtual ip in Zeus.");
            }
        } catch (Exception e) {
            loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error adding virtual ip in Zeus for loadbalancer '%d'.", dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, ZEUS_FAILURE.name(), alertDescription);
            sendErrorToEventResource(dataContainer);

            return;
        }

        Calendar eventTime = Calendar.getInstance();

        // Notify usage processor
        try {
            usageEventCollection.collectUsageAndProcessUsageRecords(dbLoadBalancer, UsageEvent.CREATE_VIRTUAL_IP, eventTime);
        } catch (UsageEventCollectionException uex) {
            LOG.error(String.format("Collection and processing of the usage event failed for load balancer: %s " +
                    ":: Exception: %s", dbLoadBalancer.getId(), uex));
        } catch (Exception exc) {
            String exceptionStackTrace = Debug.getExtendedStackTrace(exc);
            String alertDescription = String.format("An error occurred while processing the usage for an event on loadbalancer %d",
                    dbLoadBalancer.getId());
            String alertDescriptionLog = String.format("%s %d: \n%s\n\n%s",
                    alertDescription, dbLoadBalancer.getId(), exc.getMessage(), exceptionStackTrace);
            LOG.error(alertDescriptionLog);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), exc, USAGE_FAILURE.name(), alertDescription);
        }

        // Update load balancer in DB
        loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ACTIVE);

        // Add atom entry
        for (Integer newVipId : dataContainer.getNewVipIds()) {
            notificationService.saveVirtualIpEvent(dataContainer.getUserName(), dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), newVipId, CREATE_VIP_TITLE, EntryHelper.createVirtualIpSummary(), EventType.CREATE_VIRTUAL_IP, CREATE, INFO);
        }

        LOG.info(String.format("Add virtual ip operation complete for load balancer '%d'.", dbLoadBalancer.getId()));
    }

    private void sendErrorToEventResource(MessageDataContainer dataContainer) {
        String title = "Error Creating Virtual Ip";
        String desc = "Could not create the virtual ip at this time.";
        for (Integer newVipId : dataContainer.getNewVipIds()) {
            notificationService.saveVirtualIpEvent(dataContainer.getUserName(), dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), newVipId, title, desc, EventType.UPDATE_LOADBALANCER, CREATE, CRITICAL);
        }
    }
}
