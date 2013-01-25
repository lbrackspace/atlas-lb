package org.openstack.atlas.service.domain.repository;

import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.DeletedStatusException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.*;
import org.openstack.atlas.service.domain.util.Constants;
import org.openstack.atlas.util.converters.StringConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

@Repository
@Transactional
public class NodeRepository {

    final Log LOG = LogFactory.getLog(NodeRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;/**/

    public Set<Node> addNodes(LoadBalancer loadBalancer, Collection<Node> nodes) {
        Set<Node> newNodes = new HashSet<Node>();

        for (Node node : nodes) {
            node.setLoadbalancer(loadBalancer);
            newNodes.add(entityManager.merge(node));
        }

        loadBalancer.setUpdated(Calendar.getInstance());
        loadBalancer = entityManager.merge(loadBalancer);
        entityManager.flush();
        return newNodes;
    }

    public LoadBalancer delNodes(LoadBalancer lb, Collection<Node> nodes) {
        NodeMap nodeMap = new NodeMap(nodes);
        Set<Node> lbNodes = new HashSet<Node>(lb.getNodes());
        for (Node node : lbNodes) {
            Integer nodeId = node.getId();
            if (nodeMap.containsKey(nodeId)) {
                lb.getNodes().remove(node);
            }
        }
        lb.setUpdated(Calendar.getInstance());
        lb = entityManager.merge(lb);
        entityManager.flush();
        return lb;
    }

    public void deleteNodesByIds(Collection<Integer> nodeIds) {
        List<Node> nodes;
        String idsStr = StringConverter.integersAsString(nodeIds);
        String qStr = String.format("from Node n where n.id in (%s)", StringConverter.integersAsString(nodeIds));
        nodes = entityManager.createQuery(qStr).getResultList();
        for (Node node : nodes) {
            entityManager.remove(node);
        }
        entityManager.flush();
    }

    public NodeMap getNodeMap(Integer accountId, Integer loadbalancerId) {
        List<Node> nodes;
        NodeMap nodeMap = new NodeMap();
        String qStr = "from Node n where n.loadbalancer.id=:lid and n.loadbalancer.accountId=:aid";
        Query q = entityManager.createQuery(qStr).setParameter("aid", accountId).
                setParameter("lid", loadbalancerId);
        nodes = q.getResultList();
        for (Node node : nodes) {
            nodeMap.addNode(node);

        }
        return nodeMap;
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

    public Node getNodeByLoadBalancerIdIpAddressAndPort(Integer lbId, String ipAddress, Integer port) throws EntityNotFoundException {
        try {
            return (Node) entityManager.createQuery("from Node n where n.loadbalancer.id = :loadbalancerId and n.ipAddress = :ipAddress and n.port = :port").setParameter("loadbalancerId", lbId).setParameter("ipAddress", ipAddress).setParameter("port", port).getSingleResult();
        } catch (Exception e) {
            throw new EntityNotFoundException(e);
        }
    }

    public Node getNodeByAccountIdLoadBalancerIdNodeId(LoadBalancer loadBalancer,
            Integer nid) throws EntityNotFoundException, DeletedStatusException {
        if (loadBalancer.getStatus().equals(LoadBalancerStatus.DELETED)) {
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

    public Node getNodeByAccountIdLoadBalancerIdNodeId(Integer accountId, Integer loadbalancerId, Integer nodeId) throws EntityNotFoundException {
        String query = "from Node n where n.loadbalancer.id=:loadbalancerId and n.loadbalancer.accountId=:accountId AND n.id = :nodeId";
        try {
            return (Node)entityManager.createQuery(query).setParameter("accountId", accountId).setParameter("loadbalancerId",loadbalancerId).setParameter("nodeId", nodeId).getSingleResult();
        } catch (Exception e) {
            throw new EntityNotFoundException(e);
        }
    }

    // Small check for existence
    public boolean isLoadBalancerbyAccountIdLoadBalancerId(Integer aid, Integer lid) {
        String qStr;
        qStr = "SELECT l.id from LoadBalancer l";
        qStr += "    where l.accountId=:aid and l.id=:lid";
        List<Object> results = entityManager.createQuery(qStr).setParameter("aid", aid).setParameter("lid", lid).getResultList();
        if(results.size()<=0) {
            return false;
        }else{
            return true;
        }
    }

    // Get all Nodes regardless of weight status etc
    public Set<Node> getAllNodesByAccountIdLoadBalancerId(Integer aid, Integer lid) throws EntityNotFoundException {
        if(!isLoadBalancerbyAccountIdLoadBalancerId(aid, lid)){
            throw new EntityNotFoundException(String.format("Loadbalancer %d not found for account %d",lid,aid));
        }
        String qStr = "SELECT n FROM Node n where n.loadbalancer.id = :lid";
        List<Node> nodesList = entityManager.createQuery(qStr).setParameter("lid", lid).getResultList();
        Set<Node> nodes = new HashSet<Node>(nodesList);
        return nodes;
    }

    public Set<Node> getNodesByAccountIdLoadBalancerId(LoadBalancer loadBalancer,
            Integer... p) throws EntityNotFoundException, DeletedStatusException {
        if (loadBalancer.getStatus().equals(LoadBalancerStatus.DELETED)) {
            throw new DeletedStatusException("The loadbalancer is marked as deleted.");
        }
        Set<Node> nodes = loadBalancer.getNodes();
        Set<Node> nodes_out = new LinkedHashSet<Node>();
        if (p.length >= 2) {
            Integer offset = p[0];
            Integer limit = p[1];
            Integer marker = p[2];
            int i = 0;
            if (offset == null) {
                offset = 0;
            }
            if (limit == null || limit > 100) {
                limit = 100;
            }
            if (marker == null) {
                marker = 0;
            }

            for (Node node : nodes) {
                i++;
                if (node.getId() >= marker) {
                    if (i >= marker) {
                        nodes_out.add(node);
                        if (i >= marker + limit) {
                            break;
                        }
                    }
                }
            }
        } else {

            nodes_out = nodes;
        }
        return nodes_out;
    }

    public void setNodeCondition(Set<Node> nodes, String condition) {
        StringBuilder sql = new StringBuilder("Update Node set condition = '" + condition + "' where id in (");
        for (Node n : nodes) {
            sql.append(n.getId());
            sql.append(",");
        }
        sql.deleteCharAt(sql.toString().length() - 1);
        sql.append(")");
        entityManager.createQuery(sql.toString()).executeUpdate();
    }

    public void setNodeStatus(Node node) {
        NodeStatus status = node.getStatus();
        node = entityManager.find(Node.class, node.getId());
        node.setStatus(status);
        entityManager.merge(node);
    }

    public void deleteNodes(Set<Node> nodes) {
        StringBuilder sql = new StringBuilder("delete from Node  where id in (");
        for (Node n : nodes) {
            sql.append(n.getId());
            sql.append(",");
        }
        sql.deleteCharAt(sql.toString().length() - 1);
        sql.append(")");
        entityManager.createQuery(sql.toString()).executeUpdate();
    }

    public void removeNodes(int loadbalancerId) {
        entityManager.createQuery("delete from node s where s.loadbalancer.id = :lid").setParameter("lid",
                loadbalancerId).executeUpdate();
    }

    public Node save(Node node) {
        entityManager.persist(node);
        return node;
    }

    public void delete(Object o) {
        entityManager.remove(o);
        entityManager.flush();
    }

    public Object save(Object o) {
        entityManager.persist(o);
        entityManager.flush();
        return o;
    }

    // Its easier to duplicate then inject another repoisotyr here
    public LoadBalancer getLoadBalancerById(Integer id) throws EntityNotFoundException {
        LoadBalancer lb = entityManager.find(LoadBalancer.class, id);
        if (lb == null) {
            String message = Constants.LoadBalancerNotFound;
            LOG.warn(message);
            throw new EntityNotFoundException(message);
        }
        return lb;
    }
}
