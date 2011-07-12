package org.openstack.atlas.api.mgmt.async;

import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Message;

public class MgmtDeleteHostSubnetMappingListener extends BaseListener {

    private final Log LOG = LogFactory.getLog(MgmtDeleteHostSubnetMappingListener.class);

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);
        Host rHost = getEsbRequestFromMessage(message).getHost();
        Host dHost;
        try {
            dHost = hostService.getById(rHost.getId());
        } catch (EntityNotFoundException enfe) {
            return;
        }
        Hostssubnet hostssubnet = getEsbRequestFromMessage(message).getHostssubnet();
        hostssubnet.getHostsubnets().get(0).setName(dHost.getTrafficManagerName());
        reverseProxyLoadBalancerService.deleteSubnetMappings(dHost, hostssubnet);

    }
}
