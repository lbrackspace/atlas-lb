package org.openstack.atlas.adapter.itest;

import org.junit.*;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeType;
import org.rackspace.stingray.pojo.pool.Nodes_table;

import java.util.*;

import static org.openstack.atlas.service.domain.entities.NodeCondition.*;

public class SetNodesITest extends STMTestBase {
    private String vsName;
    private Set<Node> createdNodes;

    @BeforeClass
    public static void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        createSimpleLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass() {
        removeLoadBalancer();
        stmClient.destroy();
    }

    @Before
    public void standUp() throws Exception {
        vsName = ZxtmNameBuilder.genVSName(lb);
        lb.setAlgorithm(LoadBalancerAlgorithm.WEIGHTED_LEAST_CONNECTIONS);
        stmAdapter.updateLoadBalancer(config, lb, lb, null);
    }

    private void setNodes() throws Exception {
        int node3Port = 81;
        int node4Port = 82;
        int node5Port = 83;
        int node3Weight = 15;
        int node4Weight = 20;
        int node5Weight = 1;
        String node3Ip = "127.0.0.3";
        String node4Ip = "127.0.0.4";
        String node5Ip = "127.0.0.5";
        Node node3 = new Node();
        Node node4 = new Node();
        Node node5 = new Node();
        node3.setId(3);
        node4.setId(4);
        node5.setId(5);
        node3.setIpAddress(node3Ip);
        node4.setIpAddress(node4Ip);
        node5.setIpAddress(node5Ip);
        node3.setPort(node3Port);
        node4.setPort(node4Port);
        node5.setPort(node5Port);
        node3.setCondition(ENABLED);
        node4.setCondition(DISABLED);
        node5.setCondition(DRAINING);
        node3.setWeight(node3Weight);
        node4.setWeight(node4Weight);
        node5.setWeight(node5Weight);
        createdNodes = new HashSet<Node>();
        createdNodes.add(node3);
        createdNodes.add(node4);
        createdNodes.add(node5);
        lb.setNodes(createdNodes);

        createdNodes = new HashSet<Node>(createdNodes);

        stmAdapter.setNodes(config, lb);
    }

    private String getFullAddress(Node node) {
        return node.getIpAddress() + ":" + node.getPort();
    }

    @Test
    public void testSetNodes() throws Exception {
        setNodes();
        List<Nodes_table> nodes = stmClient.getPool(vsName).getProperties().getBasic().getNodes_table();
        Assert.assertEquals(3, nodes.size());
        for (Nodes_table n : nodes) {
            Node lbn = createdNodes.iterator().next();
            if (n.getNode().equals(getFullAddress(lbn))) {
                String lbncond = lbn.getCondition().toString().toLowerCase();
               Nodes_table.State state = (lbncond.equalsIgnoreCase(
                       "enabled") ? Nodes_table.State.ACTIVE : Nodes_table.State.fromValue(
                       lbn.getCondition().toString().toLowerCase()));
               Assert.assertTrue(n.getState() == state);
               Assert.assertEquals(lbn.getWeight(), n.getWeight());
               Assert.assertEquals(lbn.getIpAddress(), n.getNode().split(":")[0]);
               int type = lbn.getType().toString().equals(NodeType.PRIMARY.toString()) ? 1 : 2;
               Assert.assertEquals((int) n.getPriority(), type);
            }
        }
    }

    @Test
    public void testRemoveNode() throws Exception {
        setNodes();

        Node nodeToRemove = createdNodes.iterator().next();
        createdNodes.remove(nodeToRemove);

        stmAdapter.removeNode(config, lb, nodeToRemove);

        List<Nodes_table> nodes = stmClient.getPool(vsName).getProperties().getBasic().getNodes_table();
        for (Nodes_table n : nodes) {
            Node lbn = createdNodes.iterator().next();
            Assert.assertFalse(n.getNode() == getFullAddress(nodeToRemove));
            if (n.getNode().equals(getFullAddress(lbn))) {
                String lbncond = lbn.getCondition().toString().toLowerCase();
                Nodes_table.State state = (lbncond.equalsIgnoreCase(
                        "enabled") ? Nodes_table.State.ACTIVE : Nodes_table.State.fromValue(
                        lbn.getCondition().toString().toLowerCase()));
                Assert.assertTrue(n.getState() == state);
                Assert.assertEquals(lbn.getWeight(), n.getWeight());
                Assert.assertEquals(lbn.getIpAddress(), n.getNode().split(":")[0]);
                int type = lbn.getType().toString().equals(NodeType.PRIMARY.toString()) ? 1 : 2;
                Assert.assertEquals((int) n.getPriority(), type);
            }
        }
    }

    @Test
    public void testRemoveNodes() throws Exception {
        setNodes();

        Iterator<Node> createdNodesIterator = createdNodes.iterator();
        List<Node> nodesToRemove = new ArrayList<Node>();
        nodesToRemove.add(createdNodesIterator.next());
        nodesToRemove.add(createdNodesIterator.next());
        for (Node node : nodesToRemove) createdNodes.remove(node);

        stmAdapter.removeNodes(config, lb, nodesToRemove);

        List<Nodes_table> nodes = stmClient.getPool(vsName).getProperties().getBasic().getNodes_table();
        for (Nodes_table n : nodes) {
            Node lbn = createdNodes.iterator().next();
            for (Node ntr : nodesToRemove) {
                Assert.assertFalse(n.getNode() == getFullAddress(ntr));
            }
            if (n.getNode().equals(getFullAddress(lbn))) {
                String lbncond = lbn.getCondition().toString().toLowerCase();
                Nodes_table.State state = (lbncond.equalsIgnoreCase(
                        "enabled") ? Nodes_table.State.ACTIVE : Nodes_table.State.fromValue(
                        lbn.getCondition().toString().toLowerCase()));
                Assert.assertTrue(n.getState() == state);
                Assert.assertEquals(lbn.getWeight(), n.getWeight());
                Assert.assertEquals(lbn.getIpAddress(), n.getNode().split(":")[0]);
                int type = lbn.getType().toString().equals(NodeType.PRIMARY.toString()) ? 1 : 2;
                Assert.assertEquals((int) n.getPriority(), type);
            }
        }
    }
}
