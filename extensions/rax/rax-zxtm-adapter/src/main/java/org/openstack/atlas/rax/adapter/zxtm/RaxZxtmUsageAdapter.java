package org.openstack.atlas.rax.adapter.zxtm;

import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.UsageAdapter;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.service.domain.entity.LoadBalancer;

import java.util.List;
import java.util.Map;

public interface RaxZxtmUsageAdapter extends UsageAdapter {

    Map<Integer, Integer> getCurrentConnectionCount(LoadBalancerEndpointConfiguration config, List<LoadBalancer> lbs) throws AdapterException;
    
}
