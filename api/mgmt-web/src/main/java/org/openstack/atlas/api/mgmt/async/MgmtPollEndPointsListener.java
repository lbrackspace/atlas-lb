package org.openstack.atlas.api.mgmt.async;

import org.openstack.atlas.service.domain.entities.Host;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Message;
import java.util.List;

public class MgmtPollEndPointsListener extends BaseListener {

    final Log LOG = LogFactory.getLog(MgmtPollEndPointsListener.class);

    @Override
    public void doOnMessage(Message message) throws Exception {
        LOG.info("Entering " + getClass());
        LOG.info(message);

        boolean endpointWorks;
        List<Host> hosts = hostService.getAllHosts();
        for (Host host : hosts) {
            endpointWorks = reverseProxyLoadBalancerService.isEndPointWorking(host);
            if (endpointWorks) {
                host.setEndpointActive(Boolean.TRUE);
            } else {
                host.setEndpointActive(Boolean.FALSE);
            }
        }
    }
}

