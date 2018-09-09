package org.openstack.atlas.api.mgmt.async;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.RateLimit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Message;
import java.util.List;

public class DeleteExpiredRateLimitListener extends BaseListener {
    private final Log LOG = LogFactory.getLog(DeleteExpiredRateLimitListener.class);

    @Override
    public void doOnMessage(Message message) throws Exception {
        LOG.info("Entering " + getClass());
        LOG.info(message);

        List<RateLimit> rateLimits = rateLimitingService.retrieveLoadBalancerRateLimits();
        LOG.info("Deleting rate limits in Zeus...");
        if (!rateLimits.isEmpty()) {
            for (RateLimit rl : rateLimits) {
                LoadBalancer lb = rl.getLoadbalancer();
                if (isRestAdapter()) {
                    LOG.debug(String.format("Attempting to remove expired rate limit for load balancer in STM... '%d' ...", lb.getId()));
                    reverseProxyLoadBalancerStmService.deleteRateLimit(lb);
                    LOG.debug(String.format("expired rate limits were removed from STM..loadbalancer in STM...'%s'..", lb.getId())
                            + "Now we can remove the from the database...");
                } else {
                    LOG.debug(String.format("Attempting to remove expired rate limit for load balancer in ZXTM... '%d' ...", lb.getId()));
                    reverseProxyLoadBalancerService.deleteRateLimit(lb);
                    LOG.debug(String.format("expired rate limits were removed from ZXTM..loadbalancer...'%s'..", lb.getId()));
                }
                try {
                    rateLimitingService.removeLimitByExpiration(rl.getId());
                } catch (Exception e) {
                    LOG.error("failed to remove from database...." + e);
                }
            }
        } else {
            LOG.debug("No expired rate limits were found..");
        }
        LOG.debug("Removal of rate limits complete...");
    }
}
