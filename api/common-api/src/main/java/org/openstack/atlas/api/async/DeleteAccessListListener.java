package org.openstack.atlas.api.async;

import org.openstack.atlas.service.domain.entities.AccessList;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;

import javax.jms.Message;
import java.util.HashSet;
import java.util.Set;

import static org.openstack.atlas.service.domain.events.entities.CategoryType.DELETE;
import static org.openstack.atlas.service.domain.events.entities.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.DELETE_ACCESS_LIST;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.ZEUS_FAILURE;

public class DeleteAccessListListener extends BaseListener {

    @Override
    public void doOnMessage(Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);
        LoadBalancer queueLb = getLoadbalancerFromMessage(message);
        LoadBalancer dbLoadBalancer;

        try {
            dbLoadBalancer = loadBalancerService.get(queueLb.getId(), queueLb.getAccountId());
        } catch (EntityNotFoundException ex) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", queueLb.getId());
            notificationService.saveAlert(queueLb.getAccountId(), queueLb.getId(), ex, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb, queueLb.getAccessLists());
            return;
        }

        try {
            LOG.debug(String.format("Deleting access list for load balancer '%s' in Zeus...", dbLoadBalancer.getId()));
            reverseProxyLoadBalancerService.deleteAccessList(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId());
            LOG.debug(String.format("Access list successfully deleted for load balancer '%s' in Zeus.", dbLoadBalancer.getId()));
        } catch (Exception e) {
            loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error deleting access list in Zeus for loadbalancer '%d'.", queueLb.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, ZEUS_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb, queueLb.getAccessLists());

           return;
        }

        Set<AccessList> accessListsToDelete = new HashSet<AccessList>();
        accessListsToDelete.addAll(dbLoadBalancer.getAccessLists());
        String atomTitle = "Network Item Successfully Deleted";
        for (AccessList accessList : accessListsToDelete) {
            String atomSummary = String.format("Network Item '%d' successfully deleted", accessList.getId());
            notificationService.saveAccessListEvent(queueLb.getUserName(), queueLb.getAccountId(), queueLb.getId(), accessList.getId(), atomTitle, atomSummary, DELETE_ACCESS_LIST, DELETE, INFO);
        }
        for (AccessList accessList : accessListsToDelete) {
            LOG.debug(String.format("Removing access list item '%d' from database...", accessList.getId()));
        }

        dbLoadBalancer.getAccessLists().removeAll(accessListsToDelete);
        dbLoadBalancer.setStatus(LoadBalancerStatus.ACTIVE);
        loadBalancerService.update(dbLoadBalancer);

        //Set status record
        loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.ACTIVE);
    }

    private void sendErrorToEventResource(LoadBalancer lb, Set<AccessList> accessLists) {
        Integer itemId;
        String title = "Error Updating Access List";
        for (AccessList accessList : accessLists) {
            String desc = createAtomErrorSummary(accessList).toString();
            itemId = accessList == null ? lb.getId() : accessList.getId(); // TODO: Find a better way of dealing with null id
            notificationService.saveAccessListEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), itemId, title, desc, DELETE_ACCESS_LIST, UPDATE, CRITICAL);
        }
    }

    private StringBuilder createAtomErrorSummary(AccessList accessListItem) {
        StringBuilder atomSummary = new StringBuilder();
        atomSummary.append("Error updating access list with the following network item: ");
        atomSummary.append("address: '").append(accessListItem.getIpAddress()).append("', ");
        atomSummary.append("type: '").append(accessListItem.getType()).append("'");
        return atomSummary;
    }
}
