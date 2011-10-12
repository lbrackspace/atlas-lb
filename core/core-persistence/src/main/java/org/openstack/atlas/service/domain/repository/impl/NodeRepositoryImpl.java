package org.openstack.atlas.service.domain.repository.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.common.converters.StringConverter;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.service.domain.common.ErrorMessages;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.DeletedStatusException;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;
import org.openstack.atlas.service.domain.pojo.NodeMap;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.NodeRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigInteger;
import java.util.*;

@Repository
@Transactional
public class NodeRepositoryImpl implements NodeRepository {

    private final Log LOG = LogFactory.getLog(NodeRepositoryImpl.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    @Override
    public Set<Node> addNodes(LoadBalancer loadBalancer, Collection<Node> nodes) {
        Set<Node> newNodes = new HashSet<Node>();

        for (Node node : nodes) {
            node.setLoadBalancer(loadBalancer);
            newNodes.add(entityManager.merge(node));
        }

        loadBalancer.setUpdated(Calendar.getInstance());
        loadBalancer = entityManager.merge(loadBalancer);
        entityManager.flush();
        return newNodes;
    }

    @Override
    public LoadBalancer update(LoadBalancer loadBalancer) {
        final Set<LoadBalancerJoinVip> lbJoinVipsToLink = loadBalancer.getLoadBalancerJoinVipSet();
        loadBalancer.setLoadBalancerJoinVipSet(null);

        loadBalancer.setUpdated(Calendar.getInstance());
        loadBalancer = entityManager.merge(loadBalancer);

        // Now attach loadbalancer to vips
        for (LoadBalancerJoinVip lbJoinVipToLink : lbJoinVipsToLink) {
            VirtualIp virtualIp = entityManager.find(VirtualIp.class, lbJoinVipToLink.getVirtualIp().getId());
            LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip(loadBalancer.getPort(), loadBalancer, virtualIp);
            entityManager.merge(loadBalancerJoinVip);
            entityManager.merge(lbJoinVipToLink.getVirtualIp());
        }

        entityManager.flush();
        return loadBalancer;
    }

    public LoadBalancer deleteNodes(LoadBalancer lb, Set<Integer> nodeIds) {
        NodeMap nodeMap = new NodeMap(getNodesByIds(nodeIds));
        Set<Node> lbNodes = new HashSet<Node>(lb.getNodes());
        for(Node node : lbNodes){
            Integer nodeId = node.getId();
            if(nodeMap.containsKey(nodeId)){
                lb.getNodes().remove(node);
            }
        }
        lb.setUpdated(Calendar.getInstance());
        lb = entityManager.merge(lb);
        entityManager.flush();
        return lb;
    }

    public Node getNodesByLoadBalancer(LoadBalancer loadBalancer, Integer nid) throws EntityNotFoundException, DeletedStatusException {
        if (loadBalancer.getStatus().equals(CoreLoadBalancerStatus.DELETED)) {
            throw new DeletedStatusException("The loadbalancer is marked as deleted.");
        }

        for (Node node : loadBalancer.getNodes()) {
            if (!node.getId().equals(nid)) {
            } else {
                return node;
            }
        }
        throw new EntityNotFoundException("Node not found");
    }

    public List<Node> getNodesByIds(Collection<Integer> ids) {
        List<Node> doomedNodes = new ArrayList<Node>();
        String nodeIdsStr = StringConverter.integersAsString(ids);
        String qStr = String.format("from Node n where n.id in (%s)", nodeIdsStr);
        if (ids == null || ids.size() < 1) {
            return doomedNodes;
        }
        doomedNodes = entityManager.createQuery(qStr).getResultList();
        return doomedNodes;
    }

    public Set<Node> getNodesByAccountIdLoadBalancerId(Integer loadBalancerId, Integer accountId) throws EntityNotFoundException {

        LoadBalancer loadBalancer = new LoadBalancer();
        loadBalancer.setAccountId(accountId);
        loadBalancer.setId(loadBalancerId);

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Node> criteria = builder.createQuery(Node.class);
        Root<Node> nodeRoot = criteria.from(Node.class);
        Predicate belongsToLoadBalancer = builder.equal(nodeRoot.get(Node_.loadBalancer), loadBalancer);

        criteria.select(nodeRoot);
        criteria.where(belongsToLoadBalancer);

        try {
            return new HashSet<Node>(entityManager.createQuery(criteria).getResultList());
        } catch (Exception e) {
            LOG.error("Error executing query detected!", e);
            throw new EntityNotFoundException(e);
        }
    }

    public Node getNodeById(Integer loadBalancerId, Integer accountId, Integer nodeId) throws EntityNotFoundException {

        LoadBalancer loadBalancer = new LoadBalancer();
        loadBalancer.setAccountId(accountId);
        loadBalancer.setId(loadBalancerId);

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Node> criteria = builder.createQuery(Node.class);
        Root<Node> nodeRoot = criteria.from(Node.class);
        Predicate belongsToLoadBalancer = builder.equal(nodeRoot.get(Node_.loadBalancer), loadBalancer);
        Predicate idEquals = builder.equal(nodeRoot.get(Node_.id), nodeId);

        criteria.select(nodeRoot);
        criteria.where(belongsToLoadBalancer);
        criteria.where(idEquals);

        try {
            return entityManager.createQuery(criteria).getSingleResult();
        } catch (Exception e) {
            LOG.error("Error executing query detected!", e);
            throw new EntityNotFoundException(e);
        }
    }

    public NodeMap getNodeMap(Integer accountId, Integer loadbalancerId) throws EntityNotFoundException {
        NodeMap nodeMap = new NodeMap();
        for (Node node : getNodesByAccountIdLoadBalancerId(loadbalancerId, accountId)) {
            nodeMap.addNode(node);
        }
        return nodeMap;
    }
}
