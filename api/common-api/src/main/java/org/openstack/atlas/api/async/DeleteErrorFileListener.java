package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.services.helpers.AlertType;

import javax.jms.Message;

public class DeleteErrorFileListener extends BaseListener {
    private final Log LOG = LogFactory.getLog(DeleteErrorFileListener.class);

    public void doOnMessage(final Message message) throws Exception {
        String msg = String.format("Inside %s.doMessage", this.getClass().getName());
        LOG.debug("Entering " + getClass());
        LOG.debug(message);
        MessageDataContainer data = getDataContainerFromMessage(message);

        LOG.debug("About to remove the error file from zeus... ");
        if (data.getAccountId() != null && data.getLoadBalancerId() != null) {
            try {
             reverseProxyLoadBalancerService.removeAndSetDefaultErrorFile(data.getLoadBalancerId(),data.getAccountId());
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
