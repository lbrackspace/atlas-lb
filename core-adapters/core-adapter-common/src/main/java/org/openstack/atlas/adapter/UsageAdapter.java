package org.openstack.atlas.adapter;

import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.service.domain.entity.LoadBalancer;

import java.util.List;
import java.util.Map;

public interface UsageAdapter {

    Map<Integer, Long> getTransferBytesIn(LoadBalancerEndpointConfiguration config, List<LoadBalancer> lbs) throws AdapterException;

    Map<Integer, Long> getTransferBytesOut(LoadBalancerEndpointConfiguration config, List<LoadBalancer> lbs) throws AdapterException;
}
