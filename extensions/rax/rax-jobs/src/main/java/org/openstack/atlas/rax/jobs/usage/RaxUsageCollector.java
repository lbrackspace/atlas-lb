package org.openstack.atlas.rax.jobs.usage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.UsageAdapter;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.jobs.usage.UsageCollector;
import org.openstack.atlas.rax.adapter.zxtm.RaxZxtmUsageAdapter;
import org.openstack.atlas.service.domain.entity.LoadBalancer;

import java.util.List;
import java.util.Map;

public class RaxUsageCollector extends UsageCollector {
    private final Log LOG = LogFactory.getLog(RaxUsageCollector.class);
    protected Map<Integer, Integer> currentConnectionMap;

    public RaxUsageCollector(LoadBalancerEndpointConfiguration config, UsageAdapter usageAdapter) {
        super(config, usageAdapter);
    }

    @Override
    public void execute(List<LoadBalancer> loadBalancers) throws Exception {
        super.execute(loadBalancers);

        try {
            LOG.info(String.format("Retrieving current connection count from '%s' (%s)...", config.getHost().getName(), config.getHost().getEndpoint()));
            currentConnectionMap = ((RaxZxtmUsageAdapter) usageAdapter).getCurrentConnectionCount(config, loadBalancers);

            LOG.debug("Listing current connection counts...");
            for (Integer loadBalancerId : currentConnectionMap.keySet()) {
                LOG.debug(String.format("LB Id: '%d', Current Connection Count: %d", loadBalancerId, currentConnectionMap.get(loadBalancerId)));
            }

        } catch (AdapterException e) {
            // TODO: Discuss how to handle exceptions better
            LOG.error("Adapter exception occurred. Load balancer id(s) removed from batch. Skipping batch...", e);
            for (LoadBalancer ignoredLoadBalancer : loadBalancers) {
                LOG.error(String.format("LB id in bad batch: '%s'", ignoredLoadBalancer.getId()));
            }
        }
    }

    public Map<Integer, Integer> getCurrentConnectionMap() {
        if (currentConnectionMap == null) throw new RuntimeException("Please call execute first before retrieving data.");
        return currentConnectionMap;
    }
}
