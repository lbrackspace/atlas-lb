package org.openstack.atlas.adapter;

import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.service.domain.entity.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface LoadBalancerAdapter {

    void createLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws AdapterException;

    void deleteLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws AdapterException;

    void updateProtocol(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, LoadBalancerProtocol protocol) throws AdapterException;

    void updatePort(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Integer port) throws AdapterException;

    void setLoadBalancingAlgorithm(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, LoadBalancerAlgorithm algorithm) throws AdapterException;

    void addVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws AdapterException;

    void deleteVirtualIp(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, Integer vipId) throws AdapterException;

    void deleteVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, List<Integer> vipId) throws AdapterException;

    void changeHostForLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, Host newHost) throws AdapterException;

    void setNodes(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Collection<Node> nodes) throws AdapterException;

    void removeNodes(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, Collection<Node> nodes) throws AdapterException;

    void removeNode(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, String ipAddress, Integer port) throws AdapterException;

    void setNodeWeights(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Collection<Node> nodes) throws AdapterException;

    void updateConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, ConnectionThrottle throttle) throws AdapterException;

    void deleteConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId) throws AdapterException;

    void updateHealthMonitor(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, HealthMonitor healthMonitor) throws AdapterException;

    void removeHealthMonitor(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId) throws AdapterException;

    void suspendLoadBalancer(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId) throws AdapterException;

    void removeSuspension(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId) throws AdapterException;

    boolean isEndPointWorking(LoadBalancerEndpointConfiguration config) throws AdapterException;

    Map<String, Long> getLoadBalancerBytesIn(LoadBalancerEndpointConfiguration config, List<String> names) throws AdapterException;

    Map<String, Long> getLoadBalancerBytesOut(LoadBalancerEndpointConfiguration config, List<String> names) throws AdapterException;
}
