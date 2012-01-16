package org.openstack.atlas.jobs.usage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.UsageAdapter;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.jobs.batch.BatchAction;
import org.openstack.atlas.service.domain.entity.LoadBalancer;

import java.util.List;
import java.util.Map;

public class UsageCollector implements BatchAction<LoadBalancer> {
    private final Log LOG = LogFactory.getLog(UsageCollector.class);
    protected UsageAdapter usageAdapter;
    protected LoadBalancerEndpointConfiguration config;
    protected Map<Integer, Long> bytesInMap;
    protected Map<Integer, Long> bytesOutMap;

    public UsageCollector(LoadBalancerEndpointConfiguration config, UsageAdapter usageAdapter) {
        this.config = config;
        this.usageAdapter = usageAdapter;
    }

    @Override
    public void execute(List<LoadBalancer> loadBalancers) throws Exception {
        try {
            LOG.info(String.format("Retrieving transfer bytes in from '%s' (%s)...", config.getHost().getName(), config.getHost().getEndpoint()));
            bytesInMap = usageAdapter.getTransferBytesIn(config, loadBalancers);

            LOG.debug("Listing transfer bytes in...");
            for (Integer loadBalancerId : bytesInMap.keySet()) {
                LOG.debug(String.format("LB Id: '%d', Transfer Bytes In: %d", loadBalancerId, bytesInMap.get(loadBalancerId)));
            }

            LOG.info(String.format("Retrieving transfer bytes out from '%s' (%s)...", config.getHost().getName(), config.getHost().getEndpoint()));
            bytesOutMap = usageAdapter.getTransferBytesOut(config, loadBalancers);

            LOG.debug("Listing transfer bytes out...");
            for (Integer loadBalancerId : bytesOutMap.keySet()) {
                LOG.debug(String.format("LB Id: '%d', Transfer Bytes Out: %d", loadBalancerId, bytesOutMap.get(loadBalancerId)));
            }
        } catch (AdapterException e) {
            // TODO: Discuss how to handle exceptions better
            LOG.error("Adapter exception occurred. Load balancer id(s) removed from batch. Skipping batch...", e);
            for (LoadBalancer ignoredLoadBalancer : loadBalancers) {
                LOG.error(String.format("LB id in bad batch: '%s'", ignoredLoadBalancer.getId()));
            }
        }
    }

    public Map<Integer, Long> getBytesInMap() {
        if(bytesInMap == null) throw new RuntimeException("Please call execute first before retrieving data.");
        return bytesInMap;
    }

    public Map<Integer, Long> getBytesOutMap() {
        if(bytesOutMap == null) throw new RuntimeException("Please call execute first before retrieving data.");
        return bytesOutMap;
    }
}
