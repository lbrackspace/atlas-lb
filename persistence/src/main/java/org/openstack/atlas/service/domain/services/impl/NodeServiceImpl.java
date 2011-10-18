package org.openstack.atlas.service.domain.services.impl;

import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.pojos.NodeMap;
import org.openstack.atlas.service.domain.services.AccountLimitService;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.NodeService;
import org.openstack.atlas.service.domain.services.helpers.NodesHelper;
import org.openstack.atlas.util.converters.StringConverter;
import org.openstack.atlas.util.ip.IPv6;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.openstack.atlas.util.ip.exception.IpTypeMissMatchException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.openstack.atlas.util.ip.IPUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class NodeServiceImpl extends BaseService implements NodeService {
    private final Log LOG = LogFactory.getLog(NodeServiceImpl.class);
    private AccountLimitService accountLimitService;
    private LoadBalancerService loadBalancerService;
    
    @Required
    public void setAccountLimitService(AccountLimitService accountLimitService) {
        this.accountLimitService = accountLimitService;
    }

    @Override
    @Transactional
    public Set<Node> getNodesByAccountIdLoadBalancerId(Integer accountId, Integer loadbalancerId, Integer... p) throws EntityNotFoundException, DeletedStatusException {
        Set<Node> nodes;
        nodes = nodeRepository.getNodesByAccountIdLoadBalancerId(loadBalancerRepository.getByIdAndAccountId(loadbalancerId, accountId), p);
        return nodes;
    }

    @Override
    @Transactional
    public Node getNodeByAccountIdLoadBalancerIdNodeId(Integer aid, Integer lid, Integer nid) throws EntityNotFoundException, DeletedStatusException {
        Node node;
        node = nodeRepository.getNodeByAccountIdLoadBalancerIdNodeId(loadBalancerRepository.getByIdAndAccountId(lid, aid),nid);
        return node;
    }

    @Override
    @Transactional
    public Node getNodeByLoadBalancerIdIpAddressAndPort(Integer loadBalancerId, String ipAddress, Integer ipPort) throws EntityNotFoundException {
        return nodeRepository.getNodeByLoadBalancerIdIpAddressAndPort(loadBalancerId, ipAddress, ipPort);
    }


    @Transactional
    @Override
    public LoadBalancer delNodes(LoadBalancer lb,Collection<Node> nodes){
        LoadBalancer lbReturn = nodeRepository.delNodes(lb,nodes);
        return lbReturn;
    }

    @Transactional
    @Override
    public List<Node> getNodesByIds(Collection<Integer> ids){
        List<Node> nodes = nodeRepository.getNodesByIds(ids);
        return nodes;
    }

    @Override
    @Transactional
    public Set<Node> createNodes(LoadBalancer loadBalancer) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(loadBalancer.getId(), loadBalancer.getAccountId());
        isLbActive(dbLoadBalancer);

        Integer potentialTotalNumNodes = dbLoadBalancer.getNodes().size() + loadBalancer.getNodes().size();
        Integer nodeLimit = accountLimitService.getLimit(dbLoadBalancer.getAccountId(), AccountLimitType.NODE_LIMIT);

        if (potentialTotalNumNodes > nodeLimit) {
            throw new BadRequestException(String.format("Nodes must not exceed %d per load balancer.", nodeLimit));
        }

        LOG.debug("Verifying that there are no duplicate nodes...");
        if (detectDuplicateNodes(dbLoadBalancer, loadBalancer)) {
            LOG.warn("Duplicate nodes found! Sending failure response back to client...");
            throw new UnprocessableEntityException("Duplicate nodes detected. One or more nodes already configured on load balancer.");
        }

        if(!areAddressesValidForUse(loadBalancer.getNodes(), dbLoadBalancer)) {
            LOG.warn("Internal Ips found! Sending failure response back to client...");
            throw new BadRequestException("Invalid node address. The load balancer's virtual ip or host end point address cannot be used as a node address.");
        }

        try {
            Node badNode = blackListedItemNode(loadBalancer.getNodes());
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
        dbLoadBalancer.setStatus(LoadBalancerStatus.PENDING_UPDATE);

        LOG.debug("Current number of nodes for loadbalancer: " + dbLoadBalancer.getNodes().size());
        LOG.debug("Number of new nodes to be added: " + loadBalancer.getNodes().size());
        NodesHelper.setNodesToStatus(loadBalancer, NodeStatus.ONLINE);

        for (Node newNode : loadBalancer.getNodes()) {
            if (newNode.getWeight() == null) {
                newNode.setWeight(1);
            }
        }

        return nodeRepository.addNodes(dbLoadBalancer, loadBalancer.getNodes());
    }

    @Override
    @Transactional
    public LoadBalancer updateNode(LoadBalancer loadBalancer) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(loadBalancer.getId(), loadBalancer.getAccountId());

        Node nodeToUpdate = loadBalancer.getNodes().iterator().next();
        if (!loadBalancerContainsNode(dbLoadBalancer, nodeToUpdate)) {
            LOG.warn("Node to update not found. Sending response to client...");
            throw new EntityNotFoundException(String.format("Node with id #%d not found for loadbalancer #%d", nodeToUpdate.getId(),
                            loadBalancer.getId()));
        }

        isLbActive(dbLoadBalancer);

        Node nodeBeingUpdated = loadBalancer.getNodes().iterator().next();
        LOG.debug("Verifying that we have an at least one active node...");
        if (!activeNodeCheck(dbLoadBalancer, nodeBeingUpdated)) {
            LOG.warn("No active nodes found! Sending failure response back to client...");
            throw new UnprocessableEntityException("One or more nodes must remain ENABLED.");
        }

        LOG.debug("Nodes on dbLoadbalancer: " + dbLoadBalancer.getNodes().size());
        for (Node n : dbLoadBalancer.getNodes()) {
            if (n.getId().equals(nodeToUpdate.getId())) {
                LOG.info("Node to be updated found: " + n.getId());
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
                n.setToBeUpdated(true);
                break;
            }
        }

        LOG.debug("Updating the lb status to pending_update");
        dbLoadBalancer.setStatus(LoadBalancerStatus.PENDING_UPDATE);
        dbLoadBalancer.setUserName(loadBalancer.getUserName());

        nodeRepository.update(dbLoadBalancer);
        return dbLoadBalancer;
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

        Node nodeBeingDeleted = loadBalancer.getNodes().iterator().next();
        LOG.debug("Verifying that we have an atleast one active node...");

        if (!nodeToDeleteIsNotLastActive(dbLoadBalancer, nodeBeingDeleted)) {
            LOG.warn("Last node on lb configured as ENABLED. Sending failure response back to client...");
            throw new UnprocessableEntityException("Last node on load balancer configured as ENABLED. One or more nodes must be configured as ENABLED.");
        }

        LOG.debug("Verifying that this is not the last node");
        if (dbLoadBalancer.getNodes().size() <= 1) {
            LOG.warn("Last node! Sending failure response back to client...");
            throw new UnprocessableEntityException("Last node on the load balancer. One or more nodes must remain configured as ENABLED.");
        }


        LOG.debug("Updating the lb status to pending_update");
        dbLoadBalancer.setStatus(LoadBalancerStatus.PENDING_UPDATE);

        loadBalancerRepository.update(dbLoadBalancer);
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
                if (!NodeCondition.ENABLED.equals(n.getCondition()))
                    return false;
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

    public NodeMap getNodeMap(Integer accountId,Integer loadbalancerId){
        return nodeRepository.getNodeMap(accountId, loadbalancerId);
    }

    public List<String> prepareForNodesDeletion(Integer accountId,Integer loadBalancerId,List<Integer> ids) throws EntityNotFoundException{
            List<String> validationErrors = new ArrayList<String>();
            String format;
            String errMsg;
            LoadBalancer dlb = new LoadBalancer();
            dlb.setId(loadBalancerId);
            NodeMap nodeMap = getNodeMap(accountId, loadBalancerId);
            Set<Integer> idSet = NodeMap.listToSet(ids);
            Set<Integer> notMyIds = nodeMap.idsThatAreNotInThisMap(idSet); // Either some one elese ids or non existen ids
            Set<Integer> survivingEnabledNodes = nodeMap.nodesInConditionAfterDelete(NodeCondition.ENABLED, idSet);
            List<Node> doomedNodes = nodeMap.getNodesList(idSet);
            int doomedNodeCount = doomedNodes.size();
            int batch_delete_limit = accountLimitService.getLimit(accountId, AccountLimitType.BATCH_DELETE_LIMIT);
            if (doomedNodeCount > batch_delete_limit) {
                format = "Request to delete %d nodes exceeds the account limit"
                        + " BATCH_DELETE_LIMIT of %d please attempt to delete fewer then %d nodes";
                errMsg = String.format(format, doomedNodeCount, batch_delete_limit, batch_delete_limit);
                validationErrors.add(errMsg);
            }
            if (notMyIds.size() > 0) {
                // Don't even take this request seriously any
                // ID does not belong to this account
                format = "Node ids %s are not apart of your loadbalancer";
                errMsg = String.format(format, StringConverter.integersAsString(notMyIds));
                validationErrors.add(errMsg);
            }
            if (survivingEnabledNodes.size() < 1) {
                loadBalancerService.setStatus(dlb, LoadBalancerStatus.ACTIVE);
                errMsg = "delete node operation would result in no Enabled nodes available. You must leave at least one node enabled";
                validationErrors.add(errMsg);
            }
            return validationErrors;
    }

    public void setLoadBalancerService(LoadBalancerService loadBalancerService) {
        this.loadBalancerService = loadBalancerService;
    }

}
