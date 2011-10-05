package org.openstack.atlas.service.domain.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.Node;
import org.openstack.atlas.service.domain.exception.BadRequestException;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.ImmutableEntityException;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.NodeRepository;
import org.openstack.atlas.service.domain.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class NodeServiceImpl implements NodeService {
    private final Log LOG = LogFactory.getLog(NodeServiceImpl.class);

    @Autowired
    protected NodeRepository nodeRepository;

    @Autowired
    protected LoadBalancerRepository loadBalancerRepository;

    @Override
    @Transactional
    public Set<Node> createNodes(LoadBalancer loadBalancer) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(loadBalancer.getId(), loadBalancer.getAccountId());
//        isLbActive(dbLoadBalancer);
//
//        Integer potentialTotalNumNodes = dbLoadBalancer.getNodes().size() + loadBalancer.getNodes().size();
//        Integer nodeLimit = accountLimitService.getLimit(dbLoadBalancer.getAccountId(), AccountLimitType.NODE_LIMIT);
//
//        if (potentialTotalNumNodes > nodeLimit) {
//            throw new BadRequestException(String.format("Nodes must not exceed %d per load balancer.", nodeLimit));
//        }
//
//        LOG.debug("Verifying that there are no duplicate nodes...");
//        if (detectDuplicateNodes(dbLoadBalancer, loadBalancer)) {
//            LOG.warn("Duplicate nodes found! Sending failure response back to client...");
//            throw new UnprocessableEntityException("Duplicate nodes detected. One or more nodes already configured on load balancer.");
//        }
//
//        if (!areAddressesValidForUse(loadBalancer.getNodes(), dbLoadBalancer)) {
//            LOG.warn("Internal Ips found! Sending failure response back to client...");
//            throw new BadRequestException("Invalid node address. The load balancer's virtual ip or host end point address cannot be used as a node address.");
//        }
//
//        try {
//            Node badNode = blackListedItemNode(loadBalancer.getNodes());
//            if (badNode != null) {
//                throw new BadRequestException(String.format("Invalid node address. The address '%s' is currently not accepted for this request.", badNode.getIpAddress()));
//            }
//        } catch (IPStringConversionException ipe) {
//            LOG.warn("IPStringConversionException thrown. Sending error response to client...");
//            throw new BadRequestException("IP address was not converted properly, we are unable to process this request.");
//        } catch (IpTypeMissMatchException ipte) {
//            LOG.warn("EntityNotFoundException thrown. Sending error response to client...");
//            throw new BadRequestException("IP addresses type are mismatched, we are unable to process this request.");
//        }
//
//        LOG.debug("Updating the lb status to pending_update");
//        dbLoadBalancer.setStatus(LoadBalancerStatus.PENDING_UPDATE);
//
//        LOG.debug("Current number of nodes for loadbalancer: " + dbLoadBalancer.getNodes().size());
//        LOG.debug("Number of new nodes to be added: " + loadBalancer.getNodes().size());
//        NodesHelper.setNodesToStatus(loadBalancer, NodeStatus.ONLINE);
//
//        for (Node newNode : loadBalancer.getNodes()) {
//            if (newNode.getWeight() == null) {
//                newNode.setWeight(1);
//            }
//        }

        return nodeRepository.addNodes(dbLoadBalancer, loadBalancer.getNodes());
    }

}
