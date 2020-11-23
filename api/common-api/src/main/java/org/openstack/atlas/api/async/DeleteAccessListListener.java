package org.openstack.atlas.api.async;

import org.openstack.atlas.service.domain.entities.AccessList;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.UserPages;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;

import javax.jms.Message;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.openstack.atlas.service.domain.events.entities.CategoryType.DELETE;
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
            dbLoadBalancer = loadBalancerService.getWithUserPages(queueLb.getId(), queueLb.getAccountId());
        } catch (EntityNotFoundException ex) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", queueLb.getId());
            notificationService.saveAlert(queueLb.getAccountId(), queueLb.getId(), ex, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb, queueLb.getAccessLists());
            return;
        }

        List<Integer> accessListsToDelete = new ArrayList<Integer>();
        for (AccessList item : queueLb.getAccessLists()) {
            accessListsToDelete.add(item.getId());
        }

        try {
                LOG.debug(String.format("Deleting access list for load balancer '%s' in backend...", dbLoadBalancer.getId()));
                reverseProxyLoadBalancerVTMService.deleteAccessList(dbLoadBalancer, accessListsToDelete);
                LOG.debug(String.format("Access list successfully deleted for load balancer '%s' backend...", dbLoadBalancer.getId()));
        } catch (Exception e) {
            loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error deleting access list backend.. for loadbalancer '%d'.", queueLb.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, ZEUS_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb, queueLb.getAccessLists());

            return;
        }

        String atomTitle = "Network Item Successfully Deleted";
        for (Integer id : accessListsToDelete) {
            String atomSummary = String.format("Network Item '%d' successfully deleted", id);
            notificationService.saveAccessListEvent(queueLb.getUserName(), queueLb.getAccountId(), queueLb.getId(), id, atomTitle, atomSummary, DELETE_ACCESS_LIST, DELETE, INFO);
            LOG.debug(String.format("Removing access list item '%d' from database...", id));
        }
        Set<AccessList> saveList = new HashSet<AccessList>();
        for (AccessList item : dbLoadBalancer.getAccessLists()) {
            if (!accessListsToDelete.contains(item.getId())) {
                saveList.add(item);
            }
        }

        dbLoadBalancer.setAccessLists(saveList);
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
            notificationService.saveAccessListEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), itemId, title, desc, DELETE_ACCESS_LIST, DELETE, CRITICAL);
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
