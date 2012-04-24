package org.openstack.atlas.api.mgmt.async;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Message;

public class UpdateRateLimitListener extends BaseListener {

    final Log LOG = LogFactory.getLog(UpdateRateLimitListener.class);

    @Override
    public void doOnMessage(Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);
        LoadBalancer queueLb = getEsbRequestFromMessage(message).getLoadBalancer();
        LoadBalancer dbLoadBalancer;

        dbLoadBalancer = loadBalancerService.get(queueLb.getId());


        LOG.debug("Rate limit attributes verified. Continuing...");
        LOG.debug("Updating rate limit in Zeus...");
        reverseProxyLoadBalancerService.updateRateLimit(dbLoadBalancer, queueLb.getRateLimit());
        LOG.debug("Setting loadbalancer status to 'ACTIVE'...");
        loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ACTIVE);

        LOG.info("Update rate limit operation complete.");
    }
}
