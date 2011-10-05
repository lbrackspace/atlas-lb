package org.openstack.atlas.api.integration;

import org.openstack.atlas.service.domain.entity.*;

import java.util.Set;

public interface ReverseProxyLoadBalancerService {

    void createLoadBalancer(Integer accountId, LoadBalancer lb) throws Exception;

    void updateLoadBalancer(Integer accountId, LoadBalancer lb) throws Exception;

    void deleteLoadBalancer(Integer accountId, Integer lbId) throws Exception;

    void createNodes(Integer accountId, Integer lbId, Set<Node> nodes) throws Exception;

    void deleteNodes(Integer accountId, Integer lbId, Set<Integer> nodeIds) throws Exception;

    void updateNode(Integer accountId, Integer lbId, Node node) throws Exception;
 
    void deleteNode(Integer accountId, Integer lbId, Integer nodeId) throws Exception;

    void updateConnectionLogging(Integer accountId, Integer lbId, Boolean enabled) throws Exception;

    void updateConnectionThrottle(Integer accountId, Integer lbId, ConnectionThrottle conThrottle) throws Exception;

    void deleteConnectionThrottle(Integer accountId, Integer lbId) throws Exception;

    void updateHealthMonitor(Integer accountId, Integer lbId, HealthMonitor monitor) throws Exception;

    void deleteHealthMonitor(Integer accountId, Integer lbId) throws Exception;

    void setSessionPersistence(Integer lbId, Integer accountId, SessionPersistence sessionPersistence) throws Exception;

    void deleteSessionPersistence(Integer accountId, Integer lbId) throws Exception;
}
