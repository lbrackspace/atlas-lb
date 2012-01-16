package org.openstack.atlas.rax.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.async.BaseListener;
import org.openstack.atlas.api.atom.EntryHelper;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.datamodel.CoreUsageEventType;
import org.openstack.atlas.rax.api.integration.RaxProxyService;
import org.openstack.atlas.rax.datamodel.RaxUsageEventType;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.openstack.atlas.service.domain.entity.VirtualIpv6;
import org.openstack.atlas.service.domain.event.entity.EventType;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;

import java.util.HashSet;
import java.util.Set;

import static org.openstack.atlas.api.atom.EntryHelper.CREATE_VIP_TITLE;
import static org.openstack.atlas.service.domain.service.helpers.AlertType.*;
import static org.openstack.atlas.service.domain.event.entity.CategoryType.*;
import static org.openstack.atlas.service.domain.event.entity.EventSeverity.*;

@Component
public class RaxAddVirtualIpListener extends BaseListener {
    private final Log LOG = LogFactory.getLog(RaxAddVirtualIpListener.class);

    @Autowired
    private LoadBalancerRepository loadBalancerRepository;
    @Autowired
    private NotificationService notificationService;

    @Override
    public void doOnMessage(Message message) throws Exception {
        MessageDataContainer dataContainer = getDataContainerFromMessage(message);
        LoadBalancer dbLoadBalancer;

        try {
            dbLoadBalancer = loadBalancerRepository.getById(dataContainer.getLoadBalancerId());
            if (dataContainer.getAccountId() == null) dataContainer.setAccountId(dbLoadBalancer.getAccountId());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", dataContainer.getLoadBalancerId());
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(dataContainer);
            return;
        }

        try {
            LOG.debug(String.format("Adding virtual ips to load balancer '%d' in Zeus...", dbLoadBalancer.getId()));
            Set<VirtualIpv6> newIpv6Vips = new HashSet<VirtualIpv6>();
            newIpv6Vips.add(dataContainer.getVirtualIpv6());
            ((RaxProxyService)reverseProxyLoadBalancerService).addVirtualIps(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), new HashSet<VirtualIp>(), newIpv6Vips);
            LOG.debug("Successfully added virtual ips in Zeus.");
        } catch (Exception e) {
            loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error adding virtual ips in Zeus for loadbalancer '%d'.", dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, LBDEVICE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(dataContainer);
            return;
        }

        // Update load balancer in DB
        loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ACTIVE);

        // Add atom entry
        for (Integer newVipId : dataContainer.getNewVipIds()) {
            notificationService.saveVirtualIpEvent(dataContainer.getUserName(), dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), newVipId, CREATE_VIP_TITLE, EntryHelper.createVirtualIpSummary(), EventType.ADD_VIRTUAL_IP, CREATE, INFO);
        }

        // Notify usage processor with a usage event
        notifyUsageProcessor(message, dbLoadBalancer, RaxUsageEventType.ADD_VIRTUAL_IP);

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
