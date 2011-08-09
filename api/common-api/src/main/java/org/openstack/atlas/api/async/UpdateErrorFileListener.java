package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.openstack.atlas.service.domain.pojos.MessageDataContainer;

public class UpdateErrorFileListener extends BaseListener {
    private final Log LOG = LogFactory.getLog(UpdateErrorFileListener.class);

    @Override
    public void doOnMessage(Message message) throws Exception {
        String msg = String.format("Inside %s.doMessage", this.getClass().getName());
        LOG.info(msg);
        MessageDataContainer data = getDataContainerFromMessage(message);
        String content = data.getErrorFileContents();

        if (data.getAccountId() != null && data.getLoadBalancerId() != null) {
            Integer aid = data.getAccountId();
            Integer lid = data.getLoadBalancerId();
            try {
                LOG.debug("Attempting to set error file in zeus...");
                reverseProxyLoadBalancerService.setErrorFile(lid, aid, content);
                LOG.debug("Successfully updated error file in zeus.");
            } catch (Exception e) {
                String tmpMsg = String.format("Error setting Errorfile for %d_%d", aid, lid);
                LOG.error(tmpMsg, e);
                return; //TODO: Put alert stuff here
            }
        } else {
            LOG.debug("Attempting to set default error file in zeus...");
            reverseProxyLoadBalancerService.uploadDefaultErrorFile(hostService.getAllHosts().get(0), content);
            LOG.debug("Successfully updated default error file in zeus.");
        }

    }

}
