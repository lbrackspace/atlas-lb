package org.openstack.atlas.rax.api.async;

import org.openstack.atlas.api.async.BaseListener;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.rax.api.integration.RaxProxyService;
import org.openstack.atlas.rax.domain.entity.RaxAccessList;
import org.openstack.atlas.rax.domain.entity.RaxLoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.NotificationService;
import org.openstack.atlas.service.domain.service.helpers.AlertType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import java.util.HashSet;
import java.util.Set;

import static org.openstack.atlas.service.domain.event.entity.CategoryType.DELETE;
import static org.openstack.atlas.service.domain.event.entity.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.event.entity.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.event.entity.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.event.entity.EventType.DELETE_ACCESS_LIST;
import static org.openstack.atlas.service.domain.service.helpers.AlertType.LBDEVICE_FAILURE;

@Component
public class RaxDeleteAccessListListener extends BaseListener {
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
        } catch (EntityNotFoundException ex) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", dataContainer.getLoadBalancerId());
            LOG.error(alertDescription, ex);
            notificationService.saveAlert(dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), ex, AlertType.DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(loadBalancer, loadBalancer.getAccessLists());
            return;
        }

        try {
            LOG.debug(String.format("Deleting access list for load balancer '%s' in LB Device...", dbLoadBalancer.getId()));
            ((RaxProxyService)reverseProxyLoadBalancerService).deleteAccessList(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId());
            LOG.debug(String.format("Access list successfully deleted for load balancer '%s' in LB Device.", dbLoadBalancer.getId()));
        } catch (Exception e) {
            loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error deleting access list in LB Device for loadbalancer '%d'.", loadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, LBDEVICE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(loadBalancer, loadBalancer.getAccessLists());
            return;
        }

        Set<RaxAccessList> accessListsToDelete = new HashSet<RaxAccessList>();
        accessListsToDelete.addAll(dbLoadBalancer.getAccessLists());
        String atomTitle = "Network Item Successfully Deleted";
        for (RaxAccessList accessList : accessListsToDelete) {
            String atomSummary = String.format("Network Item '%d' successfully deleted", accessList.getId());
            notificationService.saveAccessListEvent(loadBalancer.getUserName(), loadBalancer.getAccountId(), loadBalancer.getId(), accessList.getId(), atomTitle, atomSummary, DELETE_ACCESS_LIST, DELETE, INFO);
        }

        dbLoadBalancer.getAccessLists().removeAll(accessListsToDelete);
        loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ACTIVE);
    }

    private void sendErrorToEventResource(LoadBalancer lb, Set<RaxAccessList> accessLists) {
        Integer itemId;
        String title = "Error Updating Access List";
        for (RaxAccessList accessList : accessLists) {
            String desc = createAtomErrorSummary(accessList).toString();
            itemId = accessList == null ? lb.getId() : accessList.getId(); // TODO: Find a better way of dealing with null id
            notificationService.saveAccessListEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), itemId, title, desc, DELETE_ACCESS_LIST, UPDATE, CRITICAL);
        }
    }

    private StringBuffer createAtomErrorSummary(RaxAccessList accessListItem) {
        StringBuffer atomSummary = new StringBuffer();
        atomSummary.append("Error updating access list with the following network item: ");
        atomSummary.append("address: '").append(accessListItem.getIpAddress()).append("', ");
        atomSummary.append("type: '").append(accessListItem.getType()).append("'");
        return atomSummary;
    }
}
