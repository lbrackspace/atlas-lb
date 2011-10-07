package org.openstack.atlas.service.domain.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.common.ip.exception.IPStringConversionException1;
import org.openstack.atlas.common.ip.exception.IpTypeMissMatchException;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.datamodel.CoreNodeStatus;
import org.openstack.atlas.service.domain.common.NodesHelper;
import org.openstack.atlas.service.domain.entity.AccountLimitType;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entity.Node;
import org.openstack.atlas.service.domain.exception.BadRequestException;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.ImmutableEntityException;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.NodeRepository;
import org.openstack.atlas.service.domain.service.AccountLimitService;
import org.openstack.atlas.service.domain.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class NodeServiceImpl extends BaseService implements NodeService {
    private final Log LOG = LogFactory.getLog(NodeServiceImpl.class);

    @Autowired
    protected NodeRepository nodeRepository;

    @Autowired
    protected LoadBalancerRepository loadBalancerRepository;

    @Autowired
    protected AccountLimitService accountLimitService;

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

        if (!areAddressesValidForUse(loadBalancer.getNodes(), dbLoadBalancer)) {
            LOG.warn("Internal Ips found! Sending failure response back to client...");
            throw new BadRequestException("Invalid node address. The load balancer's virtual ip or host end point address cannot be used as a node address.");
        }

        try {
            Node badNode = blackListedItemNode(loadBalancer.getNodes());
            if (badNode != null) {
                throw new BadRequestException(String.format("Invalid node address. The address '%s' is currently not accepted for this request.", badNode.getAddress()));
            }
        } catch (IPStringConversionException1 ipe) {
            LOG.warn("IPStringConversionException thrown. Sending error response to client...");
            throw new BadRequestException("IP address was not converted properly, we are unable to process this request.");
        } catch (IpTypeMissMatchException ipte) {
            LOG.warn("EntityNotFoundException thrown. Sending error response to client...");
            throw new BadRequestException("IP addresses type are mismatched, we are unable to process this request.");
        }

        LOG.debug("Updating the lb status to pending_update");
        dbLoadBalancer.setStatus(CoreLoadBalancerStatus.PENDING_UPDATE);

        LOG.debug("Current number of nodes for loadbalancer: " + dbLoadBalancer.getNodes().size());
        LOG.debug("Number of new nodes to be added: " + loadBalancer.getNodes().size());
        NodesHelper.setNodesToStatus(loadBalancer, CoreNodeStatus.ONLINE);

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
                if (nodeToUpdate.isEnabled() != null) {
                    n.isEnabled(nodeToUpdate.isEnabled());
                }
                if (nodeToUpdate.getAddress() != null) {
                    n.setAddress(nodeToUpdate.getAddress());
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
        dbLoadBalancer.setStatus(CoreLoadBalancerStatus.PENDING_UPDATE);
        dbLoadBalancer.setUserName(loadBalancer.getUserName());

        nodeRepository.update(dbLoadBalancer);
        return dbLoadBalancer;
    }

    @Override
    public boolean detectDuplicateNodes(LoadBalancer dbLoadBalancer, LoadBalancer queueLb) {
        Set<String> ipAddressesAndPorts = new HashSet<String>();
        for (Node dbNode : dbLoadBalancer.getNodes()) {
            ipAddressesAndPorts.add(dbNode.getAddress() + ":" + dbNode.getPort());
        }
        for (Node queueNode : queueLb.getNodes()) {
            if   (!ipAddressesAndPorts.add(queueNode.getAddress() + ":" + queueNode.getPort())) return true;
        }
        return false;
    }

    @Override
    public boolean areAddressesValidForUse(Set<Node> nodes, LoadBalancer dbLb) {
        for (LoadBalancerJoinVip loadBalancerJoinVip : dbLb.getLoadBalancerJoinVipSet()) {
            for (Node node : nodes) {
                if (loadBalancerJoinVip.getVirtualIp().getAddress().equals(node.getAddress())) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean activeNodeCheck(LoadBalancer dbLb, Node n) {
        List<Node> nodeList = new ArrayList<Node>();
        Node updateNode = new Node();
        for (Node node : dbLb.getNodes()) {
            if (node.isEnabled()) {
                nodeList.add(node);
                if (node.getId().equals(n.getId())) {
                    updateNode.setEnabled(node.isEnabled());
                }
            }
        }
        if (nodeList.size() <= 1) {
            if (updateNode.isEnabled() != null) {
                if (!n.isEnabled())
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
}
