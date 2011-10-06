package org.openstack.atlas.rax.adapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerAdapter;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.service.domain.entity.*;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Set;

@Primary
@Service
public class ZeusAdapterImpl implements LoadBalancerAdapter {
    public static Log LOG = LogFactory.getLog(ZeusAdapterImpl.class.getName());

    @Override
    public void createLoadBalancer(LoadBalancerEndpointConfiguration config, Integer accountId, LoadBalancer lb) throws AdapterException {
    }

    @Override
    public void updateLoadBalancer(LoadBalancerEndpointConfiguration config, Integer accountId, LoadBalancer lb) throws AdapterException {
    }

    @Override
    public void deleteLoadBalancer(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId) throws AdapterException {
    }

    @Override
    public void createNodes(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Set<Node> nodes) throws AdapterException {
    }

    @Override
    public void deleteNodes(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Set<Integer> nodeIds) throws AdapterException {
    }

    @Override
    public void updateNode(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Node node) throws AdapterException {
    }

    @Override
    public void deleteNode(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Integer nodeId) throws AdapterException {
    }

    @Override
    public void updateConnectionLogging(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Boolean enabled) throws AdapterException {
    }

    @Override
    public void updateConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, ConnectionThrottle connectionThrottle) throws AdapterException {
    }

    @Override
    public void deleteConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId) throws AdapterException {
    }

    @Override
    public void updateHealthMonitor(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, HealthMonitor monitor) throws AdapterException {
    }

    @Override
    public void deleteHealthMonitor(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId) throws AdapterException {
    }

    @Override
    public void setSessionPersistence(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, SessionPersistence sessionPersistence) throws AdapterException {
    }

    @Override
    public void deleteSessionPersistence(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId) throws AdapterException {
    }
}
