package org.openstack.atlas.api.integration;

import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.service.domain.entity.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ReverseProxyLoadBalancerService {

    void createLoadBalancer(LoadBalancer lb) throws Exception;

    void deleteLoadBalancer(LoadBalancer lb) throws Exception;

    void updateAlgorithm(LoadBalancer lb) throws Exception;

    void updatePort(LoadBalancer lb) throws Exception;

    void updateProtocol(LoadBalancer lb) throws Exception;

    void changeHostForLoadBalancer(LoadBalancer lb, Host newHost) throws Exception;

    void setNodes(Integer id, Integer accountId, Set<Node> nodes) throws Exception;

    void removeNode(Integer id, Integer accountId, Node node) throws Exception;

    void removeNodes(Integer lbId, Integer accountId, Collection<Node> nodes) throws Exception;

    void setNodeWeights(Integer id, Integer accountId, Set<Node> nodes) throws Exception;

    void updateConnectionThrottle(Integer id, Integer accountId, ConnectionThrottle connectionThrottle) throws Exception;

    void deleteConnectionThrottle(Integer id, Integer accountId) throws Exception;

    void updateHealthMonitor(Integer lbId, Integer accountId, HealthMonitor monitor) throws Exception;

    void removeHealthMonitor(Integer lbId, Integer accountId) throws Exception;

    void suspendLoadBalancer(Integer id, Integer accountId) throws Exception;

    void removeSuspension(Integer id, Integer accountId) throws Exception;

    void addVirtualIps(Integer id, Integer accountId, LoadBalancer loadBalancer) throws Exception;

    void deleteVirtualIp(LoadBalancer lb, Integer id) throws Exception;

    void deleteVirtualIps(LoadBalancer lb, List<Integer> ids) throws Exception;

    public boolean isEndPointWorking(Host host) throws Exception;

    LoadBalancerEndpointConfiguration getConfig(Host host) throws Exception;

    public LoadBalancerEndpointConfiguration getConfigHost(Host host) throws Exception;
}
