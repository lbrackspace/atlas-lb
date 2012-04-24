package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeMeta;
import org.openstack.atlas.service.domain.entities.NodeMeta_;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

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

        node.getLoadbalancer().setUpdated(Calendar.getInstance());
        node = entityManager.merge(node);
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

    public Node deleteMetadata(Node node, Collection<Integer> ids) {
        List<NodeMeta> nodeMetas = new ArrayList<NodeMeta>(node.getNodeMetadata());
        for (NodeMeta nodeMeta : nodeMetas) {
            for (Integer id : ids) {
                if (nodeMeta.getId().equals(id)) {
                    node.getNodeMetadata().remove(id);
                }
            }
        }
        node.getLoadbalancer().setUpdated(Calendar.getInstance());
        node = entityManager.merge(node);
        node.setLoadbalancer(entityManager.merge(node.getLoadbalancer()));
        entityManager.flush();
        return node;
    }
}