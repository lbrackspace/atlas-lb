package org.openstack.atlas.api.async;

import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.services.helpers.AlertType;


import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.ZEUS_FAILURE;
import static org.openstack.atlas.service.domain.events.entities.CategoryType.CREATE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.CREATE_NODE;
import static org.openstack.atlas.api.atom.EntryHelper.CREATE_NODE_TITLE;

import javax.jms.Message;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;

public class DeleteErrorFileListener extends BaseListener {

    private final Log LOG = LogFactory.getLog(DeleteErrorFileListener.class);

    public void doOnMessage(final Message message) throws Exception {
        String msg = String.format("Inside %s.doMessage", this.getClass().getName());
        LOG.debug("Entering " + getClass());
        LOG.debug(message);
        MessageDataContainer data = getDataContainerFromMessage(message);

        LoadBalancer dbLoadBalancer;

        try {
            dbLoadBalancer = loadBalancerService.get(data.getLoadBalancerId(), data.getAccountId());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", data.getLoadBalancerId());
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(data.getAccountId(), data.getLoadBalancerId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            return;
        }






        LOG.debug("About to remove the error file from zeus... ");
        if (data.getAccountId() != null && data.getLoadBalancerId() != null) {
            try {
                reverseProxyLoadBalancerService.removeAndSetDefaultErrorFile(dbLoadBalancer);
            } catch (Exception e) {
                String tmpMsg = String.format("Error setting Errorfile for %d_%d", data.getAccountId(), data.getLoadBalancerId());
                LOG.error(tmpMsg, e);
                notificationService.saveAlert(data.getAccountId(), data.getLoadBalancerId(), e, AlertType.ZEUS_FAILURE.name(), msg);
                return;
            }
        } else {
            LOG.error("Error LoadbalancerId or accountId was null in call to DeleteErrorFileListener");
        }
        LOG.debug("Successfully removed the error file from zeus... ");
    }
}
