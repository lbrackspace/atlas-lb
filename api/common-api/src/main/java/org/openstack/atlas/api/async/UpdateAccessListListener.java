package org.openstack.atlas.api.async;

import org.openstack.atlas.service.domain.entities.AccessList;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.api.atom.EntryHelper;

import javax.jms.Message;
import java.util.Set;

import static org.openstack.atlas.service.domain.events.entities.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.UPDATE_ACCESS_LIST;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.LBDEVICE_FAILURE;
import static org.openstack.atlas.api.atom.EntryHelper.UPDATE_ACCESS_LIST_TITLE;

public class UpdateAccessListListener extends BaseListener {

    @Override
    public void doOnMessage(Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);
        LoadBalancer queueLb = getLoadbalancerFromMessage(message);
        LoadBalancer dbLoadBalancer;

        try {
            dbLoadBalancer = loadBalancerService.get(queueLb.getId(), queueLb.getAccountId());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", queueLb.getId());
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(queueLb.getAccountId(), queueLb.getId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb, queueLb.getAccessLists());
            return;
        }

        try {
            LOG.debug(String.format("Update access list for load balancer '%d' in LB Device...", dbLoadBalancer.getId()));
            reverseProxyLoadBalancerService.updateAccessList(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getAccessLists());
            LOG.debug(String.format("Successfully updated access list in LB Device for load balancer '%d'.", dbLoadBalancer.getId()));
        } catch (Exception e) {
            loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error updating access list for load balancer '%d' in LB Device.", dbLoadBalancer.getId());
            LOG.error(alertDescription, e);

            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, LBDEVICE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb, queueLb.getAccessLists());
            return;
        }

        loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ACTIVE);

        for (AccessList al : accessListService.diffRequestAccessListWithDomainAccessList(queueLb, dbLoadBalancer)) {
            notificationService.saveAccessListEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), al.getId(), UPDATE_ACCESS_LIST_TITLE, EntryHelper.createAccessListSummary(al), UPDATE_ACCESS_LIST, UPDATE, INFO);
        }

        LOG.info(String.format("Update access list operation complete for load balancer '%d'.", dbLoadBalancer.getId()));
    }

    private void sendErrorToEventResource(LoadBalancer lb, Set<AccessList> accessLists) {
        Integer itemId;
        String title = "Error Updating Access List";
        for (AccessList accessList : accessLists) {
            String desc = createAtomErrorSummary(accessList).toString();
            itemId = accessList == null ? lb.getId() : accessList.getId(); // TODO: Find a better way of dealing with null id
            if(itemId == null) {
                itemId = -1;
                // TODO: Yea no kidding
            }
            notificationService.saveAccessListEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), itemId, title, desc, UPDATE_ACCESS_LIST, UPDATE, CRITICAL);
        }
    }

    private StringBuffer createAtomErrorSummary(AccessList accessListItem) {
        StringBuffer atomSummary = new StringBuffer();
        atomSummary.append("Error updating access list with the following network item: ");
        atomSummary.append("address: '").append(accessListItem.getIpAddress()).append("', ");
        atomSummary.append("type: '").append(accessListItem.getType()).append("'");
        return atomSummary;
    }
}
