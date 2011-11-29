package org.openstack.atlas.rax.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.async.BaseListener;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.datamodel.CoreUsageEventType;
import org.openstack.atlas.rax.api.integration.RaxProxyService;
import org.openstack.atlas.rax.datamodel.RaxUsageEventType;
import org.openstack.atlas.rax.domain.service.RaxVirtualIpService;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.event.entity.EventType;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.NotificationService;
import org.openstack.atlas.service.domain.service.VirtualIpService;
import org.openstack.atlas.service.domain.service.helpers.AlertType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import java.util.List;

import static org.openstack.atlas.service.domain.event.entity.CategoryType.*;
import static org.openstack.atlas.service.domain.event.entity.EventSeverity.*;

@Component
public class RaxRemoveVirtualIpsListener extends BaseListener {
    private final Log LOG = LogFactory.getLog(RaxRemoveVirtualIpsListener.class);

    @Autowired
    private LoadBalancerRepository loadBalancerRepository;
    @Autowired
    private VirtualIpService virtualIpService;
    @Autowired
    private NotificationService notificationService;

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);

        MessageDataContainer dataContainer = getDataContainerFromMessage(message);
        LoadBalancer dbLoadBalancer;
        List<Integer> vipIdsToDelete = dataContainer.getIds();

        try {
            dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(dataContainer.getLoadBalancerId(), dataContainer.getAccountId());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", dataContainer.getLoadBalancerId());
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), enfe, AlertType.DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(dataContainer);
            return;
        }

        try {
            LOG.debug(String.format("Removing virtual ips from load balancer '%d' in Zeus...", dbLoadBalancer.getId()));
            ((RaxProxyService)reverseProxyLoadBalancerService).deleteVirtualIps(dbLoadBalancer, vipIdsToDelete);
            LOG.debug(String.format("Successfully removed virtual ips from load balancer '%d' in Zeus.", dbLoadBalancer.getId()));
        } catch (Exception e) {
            loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error deleting virtual ips in Zeus for loadbalancer '%d'.", dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, AlertType.LBDEVICE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(dataContainer);
            return;
        }

        try {
            LOG.debug(String.format("Removing virtual ips from load balancer '%d' in database...", dbLoadBalancer.getId()));
            ((RaxVirtualIpService) virtualIpService).removeVipsFromLoadBalancer(dbLoadBalancer, vipIdsToDelete);
            LOG.debug(String.format("Successfully removed virtual ips from load balancer '%d' in database.", dbLoadBalancer.getId()));
        } catch (Exception e) {
            loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error deleting virtual ips in database for loadbalancer '%d'.", dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, AlertType.DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(dataContainer);
            return;
        }

        // Update load balancer status in DB
        loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ACTIVE);

        // Add atom entry
        sendSuccessToEventResource(dataContainer);

        // Notify usage processor with a usage event
        notifyUsageProcessor(message, dbLoadBalancer, RaxUsageEventType.REMOVE_VIRTUAL_IP);

        LOG.info(String.format("Delete virtual ip operation complete for load balancer '%d'.", dbLoadBalancer.getId()));
    }

    private void sendErrorToEventResource(MessageDataContainer dataContainer) {
        String title = "Error Deleting Virtual Ip";
        String desc = "Could not delete the virtual ip at this time.";
        for (Integer vipIdToDelete : dataContainer.getIds()) {
            notificationService.saveVirtualIpEvent(dataContainer.getUserName(), dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), vipIdToDelete, title, desc, EventType.DELETE_VIRTUAL_IP, DELETE, CRITICAL);
        }
    }

    private void sendSuccessToEventResource(MessageDataContainer dataContainer) {
        String atomTitle = "Virtual Ip Successfully Deleted";
        String atomSummary = "Virtual ip successfully deleted";
        for (Integer vipIdToDelete : dataContainer.getIds()) {
            notificationService.saveVirtualIpEvent(dataContainer.getUserName(), dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), vipIdToDelete, atomTitle, atomSummary, EventType.DELETE_VIRTUAL_IP, DELETE, INFO);
        }
    }
}
