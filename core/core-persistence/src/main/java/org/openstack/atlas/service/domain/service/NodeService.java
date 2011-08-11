package org.openstack.atlas.service.domain.service;


import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.Node;
import org.openstack.atlas.service.domain.exception.*;
import org.openstack.atlas.service.domain.pojo.NodeMap;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface NodeService {

    public Set<Node> getNodesByAccountIdLoadBalancerId(Integer accountId, Integer loadBalancerId, Integer... p) throws EntityNotFoundException, DeletedStatusException;

    public Node getNodeByAccountIdLoadBalancerIdNodeId(Integer accountId, Integer loadBalancerId, Integer nodeId) throws EntityNotFoundException, DeletedStatusException;

    public Set<Node> createNodes(LoadBalancer loadBalancer) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;

    public LoadBalancer updateNode(LoadBalancer loadBalancer) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException;

    public void updateNodeStatus(Node node);

    public LoadBalancer deleteNode(LoadBalancer loadBalancer) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException;

    public Node getNodeByLoadBalancerIdIpAddressAndPort(Integer loadBalancerId, String ipAddress, Integer ipPort);

    public boolean detectDuplicateNodes(LoadBalancer dbLoadBalancer, LoadBalancer queueLb);

    public boolean areAddressesValidForUse(Set<Node> nodes, LoadBalancer dbLb);

    public boolean nodeToDeleteIsNotLastActive(LoadBalancer lb, Node deleteNode);

    public NodeMap getNodeMap(Integer accountId, Integer loadbalancerId);

    public List<Node> getNodesByIds(Collection<Integer> ids);

    public LoadBalancer delNodes(LoadBalancer lb, Collection<Node> nodes);

    public List<String> prepareForNodesDeletion(Integer accountId, Integer loadBalancerId, List<Integer> ids) throws EntityNotFoundException;
}
