package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.UsageEventCollectionException;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.services.helpers.AlertType;

import javax.jms.Message;
import java.util.Calendar;
import java.util.List;

import static org.openstack.atlas.service.domain.events.entities.CategoryType.DELETE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.DELETE_VIRTUAL_IP;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.ZEUS_FAILURE;

public class DeleteVirtualIpsListener extends BaseListener {
    private final Log LOG = LogFactory.getLog(DeleteVirtualIpsListener.class);

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);

        MessageDataContainer dataContainer = getDataContainerFromMessage(message);
        LoadBalancer dbLoadBalancer;
        List<Integer> vipIdsToDelete = dataContainer.getIds();

        try {
            dbLoadBalancer = loadBalancerService.get(dataContainer.getLoadBalancerId(), dataContainer.getAccountId());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", dataContainer.getLoadBalancerId());
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), enfe, AlertType.DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(dataContainer);
            return;
        }

        try {
            LOG.debug(String.format("Removing virtual ips from load balancer '%d' in Zeus...", dbLoadBalancer.getId()));
            reverseProxyLoadBalancerService.deleteVirtualIps(dbLoadBalancer, vipIdsToDelete);
            LOG.debug(String.format("Successfully removed virtual ips from load balancer '%d' in Zeus.", dbLoadBalancer.getId()));
        } catch (Exception e) {
            loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error deleting virtual ips in Zeus for loadbalancer '%d'.", dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, ZEUS_FAILURE.name(), alertDescription);
            sendErrorToEventResource(dataContainer);

            return;
        }

        try {
            LOG.debug(String.format("Removing virtual ips from load balancer '%d' in database...", dbLoadBalancer.getId()));
            virtualIpService.removeVipsFromLoadBalancer(dbLoadBalancer, vipIdsToDelete);
            LOG.debug(String.format("Successfully removed virtual ips from load balancer '%d' in database.", dbLoadBalancer.getId()));
        } catch (Exception e) {
            loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error deleting virtual ips in database for loadbalancer '%d'.", dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(dataContainer);

            //Set status record
            loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.ERROR);
            return;
        }

        Calendar eventTime = Calendar.getInstance();

        // Notify usage processor
        try {
            usageEventCollection.collectUsageAndProcessUsageRecords(dbLoadBalancer, UsageEvent.DELETE_VIRTUAL_IP, eventTime);
        } catch (UsageEventCollectionException uex) {
            LOG.error(String.format("Collection and processing of the usage event failed for load balancer: %s " +
                    ":: Exception: %s", dbLoadBalancer.getId(), uex));
        }

        // Update load balancer status in DB
        loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ACTIVE);

        // Add atom entry
        sendSuccessToEventResource(dataContainer);

        LOG.info(String.format("Delete virtual ip operation complete for load balancer '%d'.", dbLoadBalancer.getId()));
    }

    private void sendErrorToEventResource(MessageDataContainer dataContainer) {
        String title = "Error Deleting Virtual Ip";
        String desc = "Could not delete the virtual ip at this time.";
        for (Integer vipIdToDelete : dataContainer.getIds()) {
            notificationService.saveVirtualIpEvent(dataContainer.getUserName(), dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), vipIdToDelete, title, desc, DELETE_VIRTUAL_IP, DELETE, CRITICAL);
        }
    }

    private void sendSuccessToEventResource(MessageDataContainer dataContainer) {
        String atomTitle = "Virtual Ip Successfully Deleted";
        String atomSummary = "Virtual ip successfully deleted";
        for (Integer vipIdToDelete : dataContainer.getIds()) {
            notificationService.saveVirtualIpEvent(dataContainer.getUserName(), dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), vipIdToDelete, atomTitle, atomSummary, DELETE_VIRTUAL_IP, DELETE, INFO);
        }
    }
}
