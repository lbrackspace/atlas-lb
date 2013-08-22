package org.openstack.atlas.adapter.itest;

import org.junit.*;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm;
import org.openstack.atlas.service.domain.entities.Node;
import org.rackspace.stingray.client.pool.Pool;
import org.rackspace.stingray.client.pool.PoolBasic;

import java.util.*;

import static org.openstack.atlas.service.domain.entities.NodeCondition.*;

public class SetNodesITest extends STMTestBase {
    private String vsName;
    private Set<Node> createdNodes;
    private LoadBalancerAlgorithm algorithm = LoadBalancerAlgorithm.WEIGHTED_LEAST_CONNECTIONS;

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
        lb.setAlgorithm(algorithm);
        stmAdapter.updateLoadBalancer(config, lb, lb);
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

    private void setOneNode() throws Exception {
        int node3Port = 81;
        int node3Weight = 15;
        String node3Ip = "127.0.0.3";
        Node node3 = new Node();
        node3.setId(3);
        node3.setIpAddress(node3Ip);
        node3.setPort(node3Port);
        node3.setCondition(ENABLED);
        node3.setWeight(node3Weight);
        createdNodes = new HashSet<Node>();
        createdNodes.add(node3);
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

        Pool nodePool = stmClient.getPool(vsName);
        PoolBasic nodePoolBasic = nodePool.getProperties().getBasic();
        Set<String> setOfDisabledNodes = nodePoolBasic.getDisabled();
        Set<String> setOfDrainingNodes = nodePoolBasic.getDraining();
        Set<String> setOfEnabledNodes = nodePoolBasic.getNodes();
        String poolAlgorithm = nodePool.getProperties().getLoad_balancing().getAlgorithm();
        Assert.assertEquals(poolAlgorithm, algorithm.name().toLowerCase());

        int expectedDisabledNodes = 0;
        int expectedDrainingNodes = 0;
        int expectedEnabledNodes = 0;

        for (Node node : createdNodes) {
            if (node.getCondition() == DRAINING) {
                Assert.assertTrue(setOfDrainingNodes.contains(getFullAddress(node)));
                expectedDrainingNodes++;
                expectedEnabledNodes++;
            } else if (node.getCondition() == DISABLED) {
                Assert.assertTrue(setOfDisabledNodes.contains(getFullAddress(node)));
                expectedDisabledNodes++;
            } else if (node.getCondition() == ENABLED) {
                Assert.assertTrue(setOfEnabledNodes.contains(getFullAddress(node)));
                expectedEnabledNodes++;
            } else {
                Assert.fail();
            }
        }

        Assert.assertEquals(expectedEnabledNodes, setOfEnabledNodes.size());
        Assert.assertEquals(expectedDisabledNodes, setOfDisabledNodes.size());
        Assert.assertEquals(expectedDrainingNodes, setOfDrainingNodes.size());
    }

    @Test
    public void testRemoveNode() throws Exception {
        setNodes();

        Node nodeToRemove = createdNodes.iterator().next();
        createdNodes.remove(nodeToRemove);

        stmAdapter.removeNode(config, lb, nodeToRemove);

        Pool nodePool = stmClient.getPool(vsName);
        PoolBasic nodePoolBasic = nodePool.getProperties().getBasic();
        Set<String> setOfDisabledNodes = nodePoolBasic.getDisabled();
        Set<String> setOfDrainingNodes = nodePoolBasic.getDraining();
        Set<String> setOfEnabledNodes = nodePoolBasic.getNodes();
        String poolAlgorithm = nodePool.getProperties().getLoad_balancing().getAlgorithm();
        Assert.assertEquals(poolAlgorithm, algorithm.name().toLowerCase());

        int expectedDisabledNodes = 0;
        int expectedDrainingNodes = 0;
        int expectedEnabledNodes = 0;

        for (Node node : createdNodes) {
            if (node.getCondition() == DRAINING) {
                Assert.assertTrue(setOfDrainingNodes.contains(getFullAddress(node)));
                expectedDrainingNodes++;
                expectedEnabledNodes++;
            } else if (node.getCondition() == DISABLED) {
                Assert.assertTrue(setOfDisabledNodes.contains(getFullAddress(node)));
                expectedDisabledNodes++;
            } else if (node.getCondition() == ENABLED) {
                Assert.assertTrue(setOfEnabledNodes.contains(getFullAddress(node)));
                expectedEnabledNodes++;
            } else {
                Assert.fail();
            }
        }

        Assert.assertEquals(expectedEnabledNodes, setOfEnabledNodes.size());
        Assert.assertEquals(expectedDisabledNodes, setOfDisabledNodes.size());
        Assert.assertEquals(expectedDrainingNodes, setOfDrainingNodes.size());
    }

    @Test
    public void testRemoveLastNode() throws Exception {
        setOneNode();

        Node nodeToRemove = createdNodes.iterator().next();
        createdNodes.remove(nodeToRemove);

        stmAdapter.removeNode(config, lb, nodeToRemove);

        Pool nodePool = stmClient.getPool(vsName);
        PoolBasic nodePoolBasic = nodePool.getProperties().getBasic();
        Set<String> setOfDisabledNodes = nodePoolBasic.getDisabled();
        Set<String> setOfDrainingNodes = nodePoolBasic.getDraining();
        Set<String> setOfEnabledNodes = nodePoolBasic.getNodes();
        String poolAlgorithm = nodePool.getProperties().getLoad_balancing().getAlgorithm();
        Assert.assertEquals(poolAlgorithm, algorithm.name().toLowerCase());

        Assert.assertEquals(0, setOfEnabledNodes.size());
        Assert.assertEquals(0, setOfDisabledNodes.size());
        Assert.assertEquals(0, setOfDrainingNodes.size());
    }

    @Test
    public void testRemoveNodes() throws Exception {
        setNodes();

        Iterator<Node> createdNodesIterator = createdNodes.iterator();
        List<Node> nodesToRemove = new ArrayList<Node>();
        nodesToRemove.add(createdNodesIterator.next());
        nodesToRemove.add(createdNodesIterator.next());
//        createdNodes.remove(nodesToRemove);
        for (Node node : nodesToRemove) createdNodes.remove(node);

        stmAdapter.removeNodes(config, lb, nodesToRemove);

        Pool nodePool = stmClient.getPool(vsName);
        PoolBasic nodePoolBasic = nodePool.getProperties().getBasic();
        Set<String> setOfDisabledNodes = nodePoolBasic.getDisabled();
        Set<String> setOfDrainingNodes = nodePoolBasic.getDraining();
        Set<String> setOfEnabledNodes = nodePoolBasic.getNodes();
        String poolAlgorithm = nodePool.getProperties().getLoad_balancing().getAlgorithm();
        Assert.assertEquals(poolAlgorithm, algorithm.name().toLowerCase());

        int expectedDisabledNodes = 0;
        int expectedDrainingNodes = 0;
        int expectedEnabledNodes = 0;

        for (Node node : createdNodes) {
            if (node.getCondition() == DRAINING) {
                Assert.assertTrue(setOfDrainingNodes.contains(getFullAddress(node)));
                expectedDrainingNodes++;
                expectedEnabledNodes++;
            } else if (node.getCondition() == DISABLED) {
                Assert.assertTrue(setOfDisabledNodes.contains(getFullAddress(node)));
                expectedDisabledNodes++;
            } else if (node.getCondition() == ENABLED) {
                Assert.assertTrue(setOfEnabledNodes.contains(getFullAddress(node)));
                expectedEnabledNodes++;
            } else {
                Assert.fail();
            }
        }

        Assert.assertEquals(expectedEnabledNodes, setOfEnabledNodes.size());
        Assert.assertEquals(expectedDisabledNodes, setOfDisabledNodes.size());
        Assert.assertEquals(expectedDrainingNodes, setOfDrainingNodes.size());
    }
}
