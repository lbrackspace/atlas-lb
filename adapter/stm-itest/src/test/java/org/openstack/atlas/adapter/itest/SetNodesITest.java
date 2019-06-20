package org.openstack.atlas.adapter.itest;

import org.junit.*;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeType;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.pool.PoolNodesTable;

import java.util.*;

import static org.openstack.atlas.service.domain.entities.NodeCondition.*;

public class SetNodesITest extends STMTestBase {
    private String vsName;
    private Set<Node> createdNodes;

    @BeforeClass
    public static void setupClass() {
        stmClient = new StingrayRestClient();
    }

    @After
    public void destroy() {
        removeLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass() {
        teardownEverything();
    }

    @Before
    public void standUp() throws Exception {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        createSimpleLoadBalancer();
        vsName = ZxtmNameBuilder.genVSName(lb);
        lb.setAlgorithm(LoadBalancerAlgorithm.WEIGHTED_LEAST_CONNECTIONS);
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

    private String getFullAddress(Node node) {
        // Marking this as a possible place for IpHelper.createZeusIpString(node.getIpAddress(), node.getPort())
        // In this case, this whole method would go away usages would be replaced with the above
        return node.getIpAddress() + ":" + node.getPort();
    }

    @Test
    public void testSetNodes() throws Exception {
        setNodes();

        int expectedDisabledNodes = 0;
        int expectedDrainingNodes = 0;
        int expectedEnabledNodes = 0;
        Set<String> setOfDisabledNodes = new HashSet<>();
        Set<String> setOfDrainingNodes = new HashSet<>();
        Set<String> setOfEnabledNodes = new HashSet<>();

        List<PoolNodesTable> poolNodesTable = stmClient.getPool(vsName).getProperties().getBasic().getNodesTable();
        Assert.assertEquals(3, poolNodesTable.size());

        for (PoolNodesTable pnt : poolNodesTable) {
            for (Node lbn : lb.getNodes()) {
                if (pnt.getNode().contains(lbn.getIpAddress())) {
                    Assert.assertEquals(getFullAddress(lbn), pnt.getNode());
                    Assert.assertEquals(lbn.getWeight(), pnt.getWeight());
                    if (lbn.getType() == NodeType.PRIMARY) {
                        Assert.assertEquals(Integer.valueOf(2), pnt.getPriority());
                    } else {
                        Assert.assertEquals(Integer.valueOf(1), pnt.getPriority());
                    }
                    String condition = lbn.getCondition().toString().equals("ENABLED") ? "active" : lbn.getCondition().toString().toLowerCase();
                    Assert.assertEquals(PoolNodesTable.State.fromValue(condition), pnt.getState());
                    if (pnt.getState() == PoolNodesTable.State.ACTIVE) {
                        setOfEnabledNodes.add(getFullAddress(lbn));
                    }
                    if (pnt.getState() == PoolNodesTable.State.DISABLED) {
                        setOfDisabledNodes.add(getFullAddress(lbn));
                    }
                    if (pnt.getState() == PoolNodesTable.State.DRAINING) {
                        setOfDrainingNodes.add(getFullAddress(lbn));
                        setOfEnabledNodes.add(getFullAddress(lbn));
                    }
                }
            }
        }

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

        Set<String> setOfDisabledNodes = new HashSet<>();
        Set<String> setOfDrainingNodes = new HashSet<>();
        Set<String> setOfEnabledNodes = new HashSet<>();

        List<PoolNodesTable> poolNodesTable = stmClient.getPool(vsName).getProperties().getBasic().getNodesTable();
        Assert.assertEquals(2, poolNodesTable.size());

        for (PoolNodesTable pnt : poolNodesTable) {
            for (Node lbn : lb.getNodes()) {
                if (pnt.getNode().contains(lbn.getIpAddress())) {
                    Assert.assertEquals(getFullAddress(lbn), pnt.getNode());
                    Assert.assertEquals(lbn.getWeight(), pnt.getWeight());
                    if (lbn.getType() == NodeType.PRIMARY) {
                        Assert.assertEquals(Integer.valueOf(2), pnt.getPriority());
                    } else {
                        Assert.assertEquals(Integer.valueOf(1), pnt.getPriority());
                    }
                    String condition = lbn.getCondition().toString().equals("ENABLED") ? "active" : lbn.getCondition().toString().toLowerCase();
                    Assert.assertEquals(PoolNodesTable.State.fromValue(condition), pnt.getState());
                    if (pnt.getState() == PoolNodesTable.State.ACTIVE) {
                        setOfEnabledNodes.add(getFullAddress(lbn));
                    }
                    if (pnt.getState() == PoolNodesTable.State.DISABLED) {
                        setOfDisabledNodes.add(getFullAddress(lbn));
                    }
                    if (pnt.getState() == PoolNodesTable.State.DRAINING) {
                        setOfDrainingNodes.add(getFullAddress(lbn));
                        setOfEnabledNodes.add(getFullAddress(lbn));
                    }
                }
            }
        }

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
    public void testRemoveNodes() throws Exception {
        setNodes();

        Iterator<Node> createdNodesIterator = createdNodes.iterator();
        List<Node> nodesToRemove = new ArrayList<Node>();
        nodesToRemove.add(createdNodesIterator.next());
        nodesToRemove.add(createdNodesIterator.next());
//        createdNodes.remove(nodesToRemove);
        for (Node node : nodesToRemove) createdNodes.remove(node);

        stmAdapter.removeNodes(config, lb, nodesToRemove);

        Set<String> setOfDisabledNodes = new HashSet<>();
        Set<String> setOfDrainingNodes = new HashSet<>();
        Set<String> setOfEnabledNodes = new HashSet<>();

        List<PoolNodesTable> poolNodesTable = stmClient.getPool(vsName).getProperties().getBasic().getNodesTable();
        Assert.assertEquals(1, poolNodesTable.size());

        for (PoolNodesTable pnt : poolNodesTable) {
            for (Node lbn : lb.getNodes()) {
                if (pnt.getNode().contains(lbn.getIpAddress())) {
                    Assert.assertEquals(getFullAddress(lbn), pnt.getNode());
                    Assert.assertEquals(lbn.getWeight(), pnt.getWeight());
                    if (lbn.getType() == NodeType.PRIMARY) {
                        Assert.assertEquals(Integer.valueOf(2), pnt.getPriority());
                    } else {
                        Assert.assertEquals(Integer.valueOf(1), pnt.getPriority());
                    }
                    String condition = lbn.getCondition().toString().equals("ENABLED") ? "active" : lbn.getCondition().toString().toLowerCase();
                    Assert.assertEquals(PoolNodesTable.State.fromValue(condition), pnt.getState());
                    if (pnt.getState() == PoolNodesTable.State.ACTIVE) {
                        setOfEnabledNodes.add(getFullAddress(lbn));
                    }
                    if (pnt.getState() == PoolNodesTable.State.DISABLED) {
                        setOfDisabledNodes.add(getFullAddress(lbn));
                    }
                    if (pnt.getState() == PoolNodesTable.State.DRAINING) {
                        setOfDrainingNodes.add(getFullAddress(lbn));
                        setOfEnabledNodes.add(getFullAddress(lbn));
                    }
                }
            }
        }

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
