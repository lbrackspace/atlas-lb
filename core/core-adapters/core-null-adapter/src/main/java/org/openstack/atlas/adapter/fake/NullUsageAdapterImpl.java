package org.openstack.atlas.adapter.fake;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.UsageAdapter;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Primary
@Service
public class NullUsageAdapterImpl implements UsageAdapter {
    public static Log LOG = LogFactory.getLog(NullUsageAdapterImpl.class.getName());

    @Override
    public Map<Integer, Long> getTransferBytesIn(LoadBalancerEndpointConfiguration config, List<LoadBalancer> lbs) throws AdapterException {
        LOG.info("getTransferBytesIn"); // NOP
        return new HashMap<Integer, Long>();
    }

    @Override
    public Map<Integer, Long> getTransferBytesOut(LoadBalancerEndpointConfiguration config, List<LoadBalancer> lbs) throws AdapterException {
        LOG.info("getTransferBytesOut"); // NOP
        return new HashMap<Integer, Long>();
    }
}
