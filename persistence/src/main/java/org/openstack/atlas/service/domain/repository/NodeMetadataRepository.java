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
            newMetas.add(entityManager.merge(meta));
        }

        node.getNodeMetadata().addAll(newMetas);
        node.getLoadbalancer().setUpdated(Calendar.getInstance());
        entityManager.merge(node.getLoadbalancer());
        entityManager.flush();
        return newMetas;
    }

    public List<NodeMeta> getNodeMetaDataByAccountIdNodeId(Integer nodeId) {
        List<NodeMeta> list = new ArrayList<NodeMeta>();
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<NodeMeta> criteria = builder.createQuery(NodeMeta.class);
        Root<NodeMeta> nodeMetaRoot = criteria.from(NodeMeta.class);

        Node node = new Node();
        node.setId(nodeId);

        Predicate belongsToNode = builder.equal(nodeMetaRoot.get(NodeMeta_.node), node);

        criteria.select(nodeMetaRoot);
        criteria.where(belongsToNode);
        list = entityManager.createQuery(criteria).getResultList();
        return list;
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

    public NodeMeta updateNodeMeta(Node node, NodeMeta callNodeMeta) {
        for (NodeMeta nodeMeta : node.getNodeMetadata()) {
            if (nodeMeta.getId().equals(callNodeMeta.getId())) {
                nodeMeta.setKey(callNodeMeta.getKey());
                nodeMeta.setValue(callNodeMeta.getValue());
                nodeMeta.setNode(node);
            }
        }

        node.getLoadbalancer().setUpdated(Calendar.getInstance());
        entityManager.merge(node.getLoadbalancer());
        entityManager.flush();
        return callNodeMeta;
    }
}