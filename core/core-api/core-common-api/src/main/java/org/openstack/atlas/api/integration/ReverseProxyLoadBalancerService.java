package org.openstack.atlas.api.integration;

import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.core.api.v1.LoadBalancer;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.openstack.atlas.core.api.v1.LoadBalancer;
import org.openstack.atlas.core.api.v1.Node;
import org.openstack.atlas.core.api.v1.HealthMonitor;
import org.openstack.atlas.core.api.v1.ConnectionLogging;
import org.openstack.atlas.core.api.v1.ConnectionThrottle;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ReverseProxyLoadBalancerService {

    void createLoadBalancer(Integer accountId, LoadBalancer lb) throws Exception;

    void updateLoadBalancer(Integer accountId, LoadBalancer lb) throws Exception;

    void deleteLoadBalancer(Integer accountId, Integer lbId) throws Exception;

    void createNodes(Integer accountId, Integer lbId, Set<Node> nodes) throws Exception;

    void deleteNodes(Integer accountId, Integer lbId, Set<Integer> nodeIds) throws Exception;

    void updateNode(Integer accountId, Integer lbId, Node node) throws Exception;
 
    void deleteNode(Integer accountId, Integer lbId, Integer nodeId) throws Exception;

    void updateConnectionLogging(Integer accountId, Integer lbId, ConnectionLogging conLog) throws Exception;

    void updateConnectionThrottle(Integer accountId, Integer lbId, ConnectionThrottle conThrottle) throws Exception;

    void deleteConnectionThrottle(Integer accountId, Integer lbId) throws Exception;

    void updateHealthMonitor(Integer accountId, Integer lbId, HealthMonitor monitor) throws Exception;

    void deleteHealthMonitor(Integer accountId, Integer lbId) throws Exception;
}
