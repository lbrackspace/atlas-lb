package org.openstack.atlas.adapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.service.domain.entity.LoadBalancer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
