package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.pojos.NodeMap;
import org.openstack.atlas.service.domain.services.AccountLimitService;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.LoadBalancerStatusHistoryService;
import org.openstack.atlas.service.domain.services.NodeService;
import org.openstack.atlas.service.domain.services.helpers.NodesHelper;
import org.openstack.atlas.service.domain.services.helpers.NodesPrioritiesContainer;
import org.openstack.atlas.service.domain.util.Constants;
import org.openstack.atlas.util.converters.StringConverter;
import org.openstack.atlas.util.ip.IPUtils;
import org.openstack.atlas.util.ip.IPv6;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.openstack.atlas.util.ip.exception.IpTypeMissMatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class NodeServiceImpl extends BaseService implements NodeService {
    private final Log LOG = LogFactory.getLog(NodeServiceImpl.class);

    @Autowired
    private AccountLimitService accountLimitService;
    @Autowired
    private LoadBalancerService loadBalancerService;
    @Autowired
    private LoadBalancerStatusHistoryService loadBalancerStatusHistoryService;

    @Override
    @Transactional
    public Set<Node> getNodesByAccountIdLoadBalancerId(Integer accountId, Integer loadbalancerId, Integer... p) throws EntityNotFoundException, DeletedStatusException {
        Set<Node> nodes;
        nodes = nodeRepository.getNodesByAccountIdLoadBalancerId(loadBalancerRepository.getByIdAndAccountId(loadbalancerId, accountId), p);
        return nodes;
    }

    @Override
    @Transactional
    public Set<Node> getAllNodesByAccountIdLoadBalancerId(Integer accountId, Integer loadBalancerId) throws EntityNotFoundException {
        Set<Node> nodes = nodeRepository.getAllNodesByAccountIdLoadBalancerId(accountId, loadBalancerId);
        return nodes;
    }

    @Override
    @Transactional
    public Node getNodeByAccountIdLoadBalancerIdNodeId(Integer aid, Integer lid, Integer nid) throws EntityNotFoundException, DeletedStatusException {
        Node node;
        node = nodeRepository.getNodeByAccountIdLoadBalancerIdNodeId(loadBalancerRepository.getByIdAndAccountId(lid, aid), nid);
        return node;
    }

    @Override
    @Transactional
    public Node getNodeByLoadBalancerIdIpAddressAndPort(Integer loadBalancerId, String ipAddress, Integer ipPort) throws EntityNotFoundException {
        return nodeRepository.getNodeByLoadBalancerIdIpAddressAndPort(loadBalancerId, ipAddress, ipPort);
    }

    @Transactional
    @Override
    public LoadBalancer delNodes(LoadBalancer lb, Collection<Node> nodes) {
        LoadBalancer lbReturn = nodeRepository.delNodes(lb, nodes);
        return lbReturn;
    }

    @Transactional
    @Override
    public List<Node> getNodesByIds(Collection<Integer> ids) {
        List<Node> nodes = nodeRepository.getNodesByIds(ids);
        return nodes;
    }

    @Override
    @Transactional
    public Set<Node> createNodes(LoadBalancer newNodesLb) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException, LimitReachedException {
        LoadBalancer oldNodesLb = loadBalancerRepository.getByIdAndAccountId(newNodesLb.getId(), newNodesLb.getAccountId());
        isLbActive(oldNodesLb);

        Integer potentialTotalNumNodes = oldNodesLb.getNodes().size() + newNodesLb.getNodes().size();
        Integer nodeLimit = accountLimitService.getLimit(oldNodesLb.getAccountId(), AccountLimitType.NODE_LIMIT);

        if (potentialTotalNumNodes > nodeLimit) {
            throw new LimitReachedException(String.format("Nodes must not exceed %d per load balancer.", nodeLimit));
        }
        NodesPrioritiesContainer npc = new NodesPrioritiesContainer(oldNodesLb.getNodes(), newNodesLb.getNodes());

        // You are not allowed to add secondary nodes with out Some form of monitoring. B-16407
        if(npc.hasSecondary() && !loadBalancerRepository.loadBalancerHasHealthMonitor(oldNodesLb.getId())) {
            throw new BadRequestException(Constants.NoMonitorForSecNodes);
        }

        // No secondary nodes unless there are primary nodes
        if(npc.hasSecondary() && !npc.hasPrimary()) {
            throw new BadRequestException(Constants.NoPrimaryNodeError);
        }

        LOG.debug("Verifying that there are no duplicate nodes...");
        if (detectDuplicateNodes(oldNodesLb, newNodesLb)) {
            LOG.warn("Duplicate nodes found! Sending failure response back to client...");
            throw new UnprocessableEntityException("Duplicate nodes detected. One or more nodes already configured on load balancer.");
        }

        if (!areAddressesValidForUse(newNodesLb.getNodes(), oldNodesLb)) {
            LOG.warn("Internal Ips found! Sending failure response back to client...");
            throw new BadRequestException("Invalid node address. The load balancer's virtual ip or host end point address cannot be used as a node address.");
        }

        try {
            Node badNode = blackListedItemNode(newNodesLb.getNodes());
            if (badNode != null) {
                throw new BadRequestException(String.format("Invalid node address. The address '%s' is currently not accepted for this request.", badNode.getIpAddress()));
            }
        } catch (IPStringConversionException ipe) {
            LOG.warn("IPStringConversionException thrown. Sending error response to client...");
            throw new BadRequestException("IP address was not converted properly, we are unable to process this request.");
        } catch (IpTypeMissMatchException ipte) {
            LOG.warn("EntityNotFoundException thrown. Sending error response to client...");
            throw new BadRequestException("IP addresses type are mismatched, we are unable to process this request.");
        }

        LOG.debug("Updating the lb status to pending_update");
        oldNodesLb.setStatus(LoadBalancerStatus.PENDING_UPDATE);

        //Set status record
        loadBalancerStatusHistoryService.save(oldNodesLb.getAccountId(), oldNodesLb.getId(), LoadBalancerStatus.PENDING_UPDATE);

        LOG.debug("Current number of nodes for loadbalancer: " + oldNodesLb.getNodes().size());
        LOG.debug("Number of new nodes to be added: " + newNodesLb.getNodes().size());
        NodesHelper.setNodesToStatus(newNodesLb, NodeStatus.ONLINE);

        for (Node newNode : newNodesLb.getNodes()) {
            if (newNode.getWeight() == null) {
                newNode.setWeight(1);
            }
        }

        return nodeRepository.addNodes(oldNodesLb, newNodesLb.getNodes());
    }

    @Override
    @Transactional
    public LoadBalancer updateNode(LoadBalancer msgLb) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException {
        LoadBalancer oldLbNodes = loadBalancerRepository.getByIdAndAccountId(msgLb.getId(), msgLb.getAccountId());
        //Prevent hibernate flushing updated object on failure...
        loadBalancerRepository.detach(oldLbNodes);

        Node nodeToUpdate = msgLb.getNodes().iterator().next();
        if (!loadBalancerContainsNode(oldLbNodes, nodeToUpdate)) {
            LOG.warn("Node to update not found. Sending response to client...");
            throw new EntityNotFoundException(String.format("Node with id #%d not found for loadbalancer #%d", nodeToUpdate.getId(),
                    msgLb.getId()));
        }

        isLbActive(oldLbNodes);

        LOG.debug("Nodes on dbLoadbalancer: " + oldLbNodes.getNodes().size());
        for (Node n : oldLbNodes.getNodes()) {
            if (n.getId().equals(nodeToUpdate.getId())) {
                LOG.info("Node to be updated found: " + n.getId());
                if (nodeToUpdate.getType() != null) {
                    n.setType(nodeToUpdate.getType());
                }
                if (nodeToUpdate.getCondition() != null) {
                    n.setCondition(nodeToUpdate.getCondition());
                }
                if (nodeToUpdate.getIpAddress() != null) {
                    n.setIpAddress(nodeToUpdate.getIpAddress());
                }
                if (nodeToUpdate.getPort() != null) {
                    n.setPort(nodeToUpdate.getPort());
                }
                if (nodeToUpdate.getStatus() != null) {
                    n.setStatus(nodeToUpdate.getStatus());
                }
                if (nodeToUpdate.getWeight() != null) {
                    n.setWeight(nodeToUpdate.getWeight());
                }
                if (nodeToUpdate.getType() != null) {
                    n.setType(nodeToUpdate.getType());
                }
                n.setToBeUpdated(true);
                break;
            }
        }

        // Won't delete secondary nodes untill you also delete Health Monitor
        NodesPrioritiesContainer npc = new NodesPrioritiesContainer(oldLbNodes.getNodes());
        if(npc.hasSecondary() && oldLbNodes.getHealthMonitor() == null){
            throw new BadRequestException(Constants.NoMonitorForSecNodes);
        }

        // No secondary nodes unless there are primary nodes
        if(npc.hasSecondary() && !npc.hasPrimary()) {
            throw new BadRequestException(Constants.NoPrimaryNodeError);
        }

        LOG.debug("Updating the lb status to pending_update");
        oldLbNodes.setStatus(LoadBalancerStatus.PENDING_UPDATE);
        oldLbNodes.setUserName(msgLb.getUserName());
        nodeRepository.update(oldLbNodes);

        //Set status record
        loadBalancerStatusHistoryService.save(oldLbNodes.getAccountId(), oldLbNodes.getId(), LoadBalancerStatus.PENDING_UPDATE);
        return oldLbNodes;
    }

    @Override
    @Transactional
    public void updateNodeStatus(Node node) {
        nodeRepository.setNodeStatus(node);
    }

    @Override
    @Transactional
    public LoadBalancer deleteNode(LoadBalancer loadBalancer) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(loadBalancer.getId(), loadBalancer.getAccountId());

        Node nodeToDelete = loadBalancer.getNodes().iterator().next();
        if (!loadBalancerContainsNode(dbLoadBalancer, nodeToDelete)) {
            LOG.warn("Node to delete not found. Sending response to client...");
            throw new EntityNotFoundException(String.format("Node with id #%d not found for loadbalancer #%d", nodeToDelete.getId(),
                    loadBalancer.getId()));
        }

        isLbActive(dbLoadBalancer);

        LOG.debug("Updating the lb status to pending_update");
        dbLoadBalancer.setStatus(LoadBalancerStatus.PENDING_UPDATE);
        loadBalancerRepository.update(dbLoadBalancer);

        //Set status record
        loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE);
        return loadBalancer;
    }

    @Override
    public boolean detectDuplicateNodes(LoadBalancer dbLoadBalancer, LoadBalancer queueLb) {
        Set<String> ipAddressesAndPorts = new HashSet<String>();
        Boolean retVal = false;
        String string;
        IPv6 ip;
        for (Node dbNode : dbLoadBalancer.getNodes()) {
            ip = new IPv6(dbNode.getIpAddress());
            try {
                IPUtils.isValidIpv6String(ip.expand());
                string = ip.expand() + ":" + dbNode.getPort();
            } catch (IPStringConversionException ex) {
                string = dbNode.getIpAddress() + ":" + dbNode.getPort();
            }
            ipAddressesAndPorts.add(string);
        }
        for (Node queueNode : queueLb.getNodes()) {
            ip = new IPv6(queueNode.getIpAddress());
            try {
                IPUtils.isValidIpv6String(ip.expand());
                string = (ip.expand() + ":" + queueNode.getPort());
            } catch (IPStringConversionException ex) {
                string = queueNode.getIpAddress() + ":" + queueNode.getPort();
            }
            if (!ipAddressesAndPorts.add(string)) {
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public boolean areAddressesValidForUse(Set<Node> nodes, LoadBalancer dbLb) {
        for (LoadBalancerJoinVip loadBalancerJoinVip : dbLb.getLoadBalancerJoinVipSet()) {
            for (Node node : nodes) {
                if (loadBalancerJoinVip.getVirtualIp().getIpAddress().equals(node.getIpAddress())) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean nodeToDeleteIsNotLastActive(LoadBalancer lb, Node deleteNode) {
        List<Node> nodeList = new ArrayList<Node>();
        Node nNode = new Node();
        for (Node tNode : lb.getNodes()) {
            if (NodeCondition.ENABLED.equals(tNode.getCondition())) {
                nodeList.add(tNode);
            }
            if (tNode.getId().equals(deleteNode.getId())) {
                nNode.setCondition(tNode.getCondition());
            }
        }
        boolean isFalse;
        isFalse = true;
        if (NodeCondition.ENABLED.equals(nNode.getCondition())) {
            if (!(nodeList.size() <= 1)) {
                return true;
            }
            isFalse = false;
        }
        return isFalse;
    }

    private static boolean activeNodeCheck(LoadBalancer dbLb, Node n) {
        List<Node> nodeList = new ArrayList<Node>();
        Node updateNode = new Node();
        for (Node node : dbLb.getNodes()) {
            if (NodeCondition.ENABLED.equals(node.getCondition())) {
                nodeList.add(node);
                if (node.getId().equals(n.getId())) {
                    updateNode.setCondition(node.getCondition());
                }
            }
        }
        if (nodeList.size() <= 1) {
            if (updateNode.getCondition() != null) {
                if (!NodeCondition.ENABLED.equals(n.getCondition())) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean loadBalancerContainsNode(LoadBalancer lb, Node node) {
        for (Node n : lb.getNodes()) {
            if (n.getId().equals(node.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NodeMap getNodeMap(Integer accountId, Integer loadbalancerId) {
        return nodeRepository.getNodeMap(accountId, loadbalancerId);
    }

    @Override
    @Transactional
    public List<String> prepareForNodesDeletion(Integer accountId, Integer loadBalancerId, List<Integer> ids) throws EntityNotFoundException {
        List<String> validationErrors = new ArrayList<String>();
        String format;
        String errMsg;
        LoadBalancer dlb = new LoadBalancer();
        dlb.setId(loadBalancerId);
        NodeMap nodeMap = getNodeMap(accountId, loadBalancerId);
        Set<Integer> idSet = NodeMap.listToSet(ids);
        Set<Integer> notMyIds = nodeMap.idsThatAreNotInThisMap(idSet); // Either some one elese ids or non existent ids
        List<Node> doomedNodes = nodeMap.getNodesList(idSet);
        int doomedNodeCount = doomedNodes.size();
        int batch_delete_limit = accountLimitService.getLimit(accountId, AccountLimitType.BATCH_DELETE_LIMIT);
        if (doomedNodeCount > batch_delete_limit) {
            format = "Request to delete %d nodes exceeds the account limit"
                    + " BATCH_DELETE_LIMIT of %d please attempt to delete fewer then %d nodes";
            errMsg = String.format(format, doomedNodeCount, batch_delete_limit, batch_delete_limit);
            validationErrors.add(errMsg);
        }

        //Set status record
        loadBalancerStatusHistoryService.save(accountId, loadBalancerId, LoadBalancerStatus.PENDING_UPDATE);

        if (notMyIds.size() > 0) {
            // Don't even take this request seriously any
            // ID does not belong to this account
            format = "Node ids %s are not a part of your loadbalancer";
            errMsg = String.format(format, StringConverter.integersAsString(notMyIds));
            validationErrors.add(errMsg);
        }

        return validationErrors;
    }
}
