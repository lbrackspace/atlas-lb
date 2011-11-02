package org.openstack.atlas.adapter;

import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.Node;
import org.openstack.atlas.service.domain.entity.ConnectionThrottle;
import org.openstack.atlas.service.domain.entity.HealthMonitor;
import org.openstack.atlas.service.domain.entity.SessionPersistence;

import java.util.Collection;
import java.util.Set;
import java.util.Map;

public interface LoadBalancerAdapter {

    void createLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws AdapterException;

    void updateLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws AdapterException;

    void deleteLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws AdapterException;

    void createNodes(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Set<Node> nodes) throws AdapterException;

    void deleteNodes(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Set<Node> nodes) throws AdapterException;

    void updateNode(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Node node) throws AdapterException;

    void updateConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, ConnectionThrottle connectionThrottle) throws AdapterException;

    void deleteConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId) throws AdapterException;

    void updateHealthMonitor(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, HealthMonitor healthMonitor) throws AdapterException;

    void deleteHealthMonitor(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId) throws AdapterException;

    void setSessionPersistence(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, SessionPersistence sessionPersistence) throws AdapterException;

    void deleteSessionPersistence(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId) throws AdapterException;
}
