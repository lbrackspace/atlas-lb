package org.openstack.atlas.rax.api.async;

import org.openstack.atlas.api.async.BaseListener;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.rax.api.integration.RaxProxyService;
import org.openstack.atlas.rax.domain.entity.RaxAccessList;
import org.openstack.atlas.rax.domain.entity.RaxLoadBalancer;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.NotificationService;
import org.openstack.atlas.service.domain.service.helpers.AlertType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;

import static org.openstack.atlas.service.domain.event.entity.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.event.entity.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.event.entity.EventType.UPDATE_ACCESS_LIST;
import static org.openstack.atlas.service.domain.service.helpers.AlertType.DATABASE_FAILURE;

@Component
public class RaxUpdateAccessListListener extends BaseListener {

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
            String alertDescription = String.format("Load balancer '%d' not found in database.", dataContainer.getLoadBalancerId());
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(loadBalancer);
            return;
        }

        try {
            LOG.debug(String.format("Update access list for load balancer '%d' in LB Device...", dbLoadBalancer.getId()));
            ((RaxProxyService)reverseProxyLoadBalancerService).updateAccessList(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getAccessLists());
            LOG.debug(String.format("Successfully updated access list in LB Device for load balancer '%d'.", dbLoadBalancer.getId()));
        } catch (Exception e) {
            loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error updating access list for load balancer '%d' in LB Device.", dbLoadBalancer.getId());
            LOG.error(alertDescription, e);

            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, AlertType.LBDEVICE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(loadBalancer);
            return;
        }

        loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ACTIVE);

        LOG.info(String.format("Update access list operation complete for load balancer '%d'.", dbLoadBalancer.getId()));
    }

    private void sendErrorToEventResource(RaxLoadBalancer lb) {
        String title = "Error Updating Access List";
        String desc = "Could not create the node at this time";
        for (RaxAccessList accessList : lb.getAccessLists()) {
            notificationService.saveAccessListEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), accessList.getId(), title, desc, UPDATE_ACCESS_LIST, UPDATE, CRITICAL);
        }
    }
}
