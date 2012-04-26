package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeMeta;
import org.openstack.atlas.service.domain.entities.NodeMeta_;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.util.Constants;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

@Repository
@Transactional
public class NodeMetadataRepository {
    final Log LOG = LogFactory.getLog(NodeMetadataRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    public List<NodeMeta> addNodeMetas(Node node, Collection<NodeMeta> metas) {
        List<NodeMeta> newMetas = new ArrayList<NodeMeta>();

        for (NodeMeta meta : metas) {
            meta.setNode(node);
            newMetas.add(meta);
        }

        node.getNodeMetadata().addAll(newMetas);
        node.getLoadbalancer().setUpdated(Calendar.getInstance());
        entityManager.merge(node.getLoadbalancer());
        entityManager.flush();
        return newMetas;
    }

    public List<NodeMeta> getNodeMetaDataByAccountIdNodeId(Integer nodeId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<NodeMeta> criteria = builder.createQuery(NodeMeta.class);
        Root<NodeMeta> nodeMetaRoot = criteria.from(NodeMeta.class);

        Node node = new Node();
        node.setId(nodeId);

        Predicate belongsToNode = builder.equal(nodeMetaRoot.get(NodeMeta_.node), node);

        criteria.select(nodeMetaRoot);
        criteria.where(belongsToNode);
        return entityManager.createQuery(criteria).getResultList();
    }

    public void deleteMetadata(Node node, Collection<Integer> ids) throws EntityNotFoundException {
        List<NodeMeta> nodeMetas = new ArrayList<NodeMeta>();
        Boolean exists = false;

        for (NodeMeta meta : node.getNodeMetadata()) {
            if (ids.contains(meta.getId()))
                exists = true;
            else {
                nodeMetas.add(meta);
            }
        }

        if (!exists) {
            String message = Constants.MetaNotFound;
            LOG.warn(message);
            throw new EntityNotFoundException(message);
        }
        entityManager.createQuery("DELETE FROM NodeMeta n WHERE n.id IN (:ids)").setParameter("ids", ids).executeUpdate();

        node.setNodeMetadata(nodeMetas);
        node.getLoadbalancer().setUpdated(Calendar.getInstance());
        entityManager.merge(node.getLoadbalancer());
        entityManager.flush();
    }

    public NodeMeta getNodeMeta(Integer nodeId, Integer id) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<NodeMeta> criteria = builder.createQuery(NodeMeta.class);
        Root<NodeMeta> nodeMetaRoot = criteria.from(NodeMeta.class);

        Node node = new Node();
        node.setId(nodeId);

        Predicate belongsToNode = builder.equal(nodeMetaRoot.get(NodeMeta_.node), node);
        Predicate hasId = builder.equal(nodeMetaRoot.get(NodeMeta_.id), id);

        criteria.select(nodeMetaRoot);
        criteria.where(builder.and(belongsToNode, hasId));

        NodeMeta resultItem;
        try {
            resultItem = entityManager.createQuery(criteria).getSingleResult();
        } catch (Exception e) {
            return new NodeMeta();
        }
        return resultItem;
    }

    /*
        String query = "from Node n where n.loadbalancer.id=:loadbalancerId and n.loadbalancer.accountId=:accountId AND n.id = :nodeId";
        try {
            return (Node)entityManager.createQuery(query).setParameter("accountId", accountId).setParameter("loadbalancerId",loadbalancerId).setParameter("nodeId", nodeId).getSingleResult();
        } catch (Exception e) {
            throw new EntityNotFoundException(e);
        }


    public Meta getMeta(Integer accountId, Integer loadBalancerId, Integer id) throws EntityNotFoundException {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Meta> criteria = builder.createQuery(Meta.class);
        Root<Meta> metaRoot = criteria.from(Meta.class);

        LoadBalancer lb = new LoadBalancer();
        lb.setId(loadBalancerId);
        lb.setAccountId(accountId);

        Predicate belongsToLoadBalancer = builder.equal(metaRoot.get(Meta_.loadbalancer), lb);
        Predicate hasId = builder.equal(metaRoot.get(Meta_.id), id);

        criteria.select(metaRoot);
        criteria.where(builder.and(belongsToLoadBalancer, hasId));
        final List<Meta> resultList = entityManager.createQuery(criteria).getResultList();

        if (resultList.isEmpty()) {
            String message = Constants.MetaNotFound;
            LOG.warn(message);
            throw new EntityNotFoundException(message);
        }

        return resultList.get(0);
    }

    public void deleteMeta(LoadBalancer loadBalancer, Integer id) throws EntityNotFoundException {
        Set<Meta> dbMetadata = new HashSet<Meta>(loadBalancer.getMetadata());
        Boolean removed = false;

        for (Meta meta : dbMetadata) {
            Integer metaId = meta.getId();
            if (metaId.equals(id)) {
                loadBalancer.getMetadata().remove(meta);
                removed = true;
            }
        }

        if (!removed) {
            String message = Constants.MetaNotFound;
            LOG.warn(message);
            throw new EntityNotFoundException(message);
        }

        loadBalancer.setUpdated(Calendar.getInstance());
        loadBalancer = entityManager.merge(loadBalancer);
        entityManager.flush();
    }

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
     */
}