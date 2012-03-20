package org.openstack.atlas.adapter.itest;

import com.zxtm.service.client.PoolWeightingsDefinition;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.ZxtmRollBackException;
import org.openstack.atlas.adapter.helpers.IpHelper;
import org.openstack.atlas.service.domain.entities.Node;

import java.rmi.RemoteException;

import static org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm.WEIGHTED_LEAST_CONNECTIONS;
import static org.openstack.atlas.service.domain.entities.NodeCondition.DISABLED;
import static org.openstack.atlas.service.domain.entities.NodeCondition.DRAINING;
import static org.openstack.atlas.service.domain.entities.NodeCondition.ENABLED;

public class SetNodesIntegrationTest extends ZeusTestBase {

    @BeforeClass
    public static void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        setupSimpleLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass() {
        removeSimpleLoadBalancer();
    }

    @Test
    public void testNodeOperations() throws Exception {
        // Update algorithm so we can test that node weights get set properly
        zxtmAdapter.setLoadBalancingAlgorithm(config, lb.getId(), lb.getAccountId(), WEIGHTED_LEAST_CONNECTIONS);

        setNodes();
        updateNodeConditionsToEnabled();
        shouldRollbackWhenUpdatingAllNodeConditionsToDisabled();
        updateAllNodeConditionsToDraining();
        shouldRollbackWhenSettingUnsupportedNodeWeights();
        updateNodeWeights();
        removeNode();
    }

    private void setNodes() throws Exception {
        final int defaultNodeWeight = 1;
        Node node3 = new Node();
        Node node4 = new Node();
        Node node5 = new Node();
        node3.setIpAddress("127.0.0.3");
        node4.setIpAddress("127.0.0.4");
        node5.setIpAddress("127.0.0.5");
        node3.setPort(81);
        node4.setPort(82);
        node5.setPort(83);
        node3.setCondition(ENABLED);
        node4.setCondition(DISABLED);
        node5.setCondition(DRAINING);
        node3.setWeight(15);
        node4.setWeight(20);
        node5.setWeight(1);

        lb.getNodes().add(node3);
        lb.getNodes().add(node4);
        lb.getNodes().add(node5);

        zxtmAdapter.setNodes(config, lb);

        String node1ZeusString = IpHelper.createZeusIpString(node1.getIpAddress(), node1.getPort());
        String node2ZeusString = IpHelper.createZeusIpString(node2.getIpAddress(), node2.getPort());
        String node3ZeusString = IpHelper.createZeusIpString(node3.getIpAddress(), node3.getPort());
        String node4ZeusString = IpHelper.createZeusIpString(node4.getIpAddress(), node4.getPort());
        String node5ZeusString = IpHelper.createZeusIpString(node5.getIpAddress(), node5.getPort());

        final String[][] enabledNodes = getServiceStubs().getPoolBinding().getNodes(new String[]{poolName()});
        Assert.assertEquals(1, enabledNodes.length);
        Assert.assertEquals(3, enabledNodes[0].length);
        Assert.assertEquals(node1ZeusString, enabledNodes[0][0]);
        Assert.assertEquals(node3ZeusString, enabledNodes[0][1]);

        final String[][] disabledNodes = getServiceStubs().getPoolBinding().getDisabledNodes(new String[]{poolName()});
        Assert.assertEquals(1, disabledNodes.length);
        Assert.assertEquals(2, disabledNodes[0].length);
        Assert.assertEquals(node2ZeusString, disabledNodes[0][0]);
        Assert.assertEquals(node4ZeusString, disabledNodes[0][1]);

        final String[][] drainingNodes = getServiceStubs().getPoolBinding().getDrainingNodes(new String[]{poolName()});
        Assert.assertEquals(1, drainingNodes.length);
        Assert.assertEquals(1, drainingNodes[0].length);
        Assert.assertEquals(node5ZeusString, drainingNodes[0][0]);

        final PoolWeightingsDefinition[][] weightingsDefinitions = getServiceStubs().getPoolBinding().getWeightings(new String[]{poolName()});
        Assert.assertEquals(1, weightingsDefinitions.length);
        Assert.assertEquals(5, weightingsDefinitions[0].length);

        for (PoolWeightingsDefinition weightingsDefinition : weightingsDefinitions[0]) {
            if (weightingsDefinition.getNode().equals(node1ZeusString))
                Assert.assertEquals(defaultNodeWeight, weightingsDefinition.getWeighting());
            else if (weightingsDefinition.getNode().equals(node2ZeusString))
                Assert.assertEquals(defaultNodeWeight, weightingsDefinition.getWeighting());
            else if (weightingsDefinition.getNode().equals(node3ZeusString))
                Assert.assertEquals(node3.getWeight().intValue(), weightingsDefinition.getWeighting());
            else if (weightingsDefinition.getNode().equals(node4ZeusString))
                Assert.assertEquals(node4.getWeight().intValue(), weightingsDefinition.getWeighting());
            else if (weightingsDefinition.getNode().equals(node5ZeusString))
                Assert.assertEquals(node5.getWeight().intValue(), weightingsDefinition.getWeighting());
            else Assert.fail("Unrecognized node weighting definition.");
        }

        // Remove so later tests aren't affected
        lb.getNodes().remove(node3);
        lb.getNodes().remove(node4);
        lb.getNodes().remove(node5);
        zxtmAdapter.setNodes(config, lb);
    }

    private void updateNodeConditionsToEnabled() throws Exception {
        for (Node node : lb.getNodes()) {
            node.setCondition(ENABLED);
        }

        zxtmAdapter.setNodes(config, lb);

        assertThatAllNodesAreEnabled();
    }

    private void assertThatAllNodesAreEnabled() throws RemoteException, InsufficientRequestException {
        final String[][] enabledNodes = getServiceStubs().getPoolBinding().getNodes(new String[]{poolName()});
        Assert.assertEquals(1, enabledNodes.length);
        Assert.assertEquals(2, enabledNodes[0].length);

        final String[][] disabledNodes = getServiceStubs().getPoolBinding().getDisabledNodes(new String[]{poolName()});
        Assert.assertEquals(1, disabledNodes.length);
        Assert.assertEquals(0, disabledNodes[0].length);

        final String[][] drainingNodes = getServiceStubs().getPoolBinding().getDrainingNodes(new String[]{poolName()});
        Assert.assertEquals(0, drainingNodes[0].length);
    }

    private void shouldRollbackWhenUpdatingAllNodeConditionsToDisabled() throws Exception {
        for (Node node : lb.getNodes()) {
            node.setCondition(DISABLED);
        }

        try {
            assertThatAllNodesAreEnabled();
            zxtmAdapter.setNodes(config, lb);
        } catch (Exception e) {
            if (e instanceof ZxtmRollBackException) assertThatAllNodesAreEnabled();
            else Assert.fail("Expected a ZxtmRollBackException.");
        }
    }

    private void updateAllNodeConditionsToDraining() throws Exception {
        for (Node node : lb.getNodes()) {
            node.setCondition(DRAINING);
        }

        zxtmAdapter.setNodes(config, lb);

        final String[][] enabledNodes = getServiceStubs().getPoolBinding().getNodes(new String[]{poolName()});
        Assert.assertEquals(1, enabledNodes.length);
        Assert.assertEquals(2, enabledNodes[0].length);

        final String[][] disabledNodes = getServiceStubs().getPoolBinding().getDisabledNodes(new String[]{poolName()});
        Assert.assertEquals(1, disabledNodes.length);
        Assert.assertEquals(0, disabledNodes[0].length);

        final String[][] drainingNodes = getServiceStubs().getPoolBinding().getDrainingNodes(new String[]{poolName()});
        Assert.assertEquals(1, drainingNodes.length);
        Assert.assertEquals(2, drainingNodes[0].length);
    }

    private void shouldRollbackWhenSettingUnsupportedNodeWeights() throws Exception {
        node1.setWeight(0);
        node2.setWeight(101);

        try {
            zxtmAdapter.setNodeWeights(config, lb.getId(), lb.getAccountId(), lb.getNodes());
        } catch (Exception e) {
            if (e instanceof ZxtmRollBackException) {
                final String[][] enabledNodes = getServiceStubs().getPoolBinding().getNodes(new String[]{poolName()});
                final String[][] disabledNodes = getServiceStubs().getPoolBinding().getDisabledNodes(new String[]{poolName()});
                final String[][] drainingNodes = getServiceStubs().getPoolBinding().getDrainingNodes(new String[]{poolName()});

                final PoolWeightingsDefinition[][] enabledNodeWeights = getServiceStubs().getPoolBinding().getNodesWeightings(new String[]{poolName()}, enabledNodes);
                Assert.assertEquals(1, enabledNodeWeights.length);
                Assert.assertEquals(2, enabledNodeWeights[0].length);
                Assert.assertEquals(1, enabledNodeWeights[0][1].getWeighting());

                final PoolWeightingsDefinition[][] disabledNodeWeights = getServiceStubs().getPoolBinding().getNodesWeightings(new String[]{poolName()}, disabledNodes);
                Assert.assertEquals(1, disabledNodeWeights.length);
                Assert.assertEquals(0, disabledNodeWeights[0].length);

                final PoolWeightingsDefinition[][] drainingNodeWeights = getServiceStubs().getPoolBinding().getNodesWeightings(new String[]{poolName()}, drainingNodes);
                Assert.assertEquals(1, drainingNodeWeights.length);
                Assert.assertEquals(2, drainingNodeWeights[0].length);
                Assert.assertEquals(1, drainingNodeWeights[0][0].getWeighting());
                Assert.assertEquals(1, drainingNodeWeights[0][1].getWeighting());
            } else {
                Assert.fail("ZxtmRollBackException expected.");
            }
        }
    }

    private void updateNodeWeights() throws Exception {
        node1.setWeight(50);
        node2.setWeight(100);

        zxtmAdapter.setNodeWeights(config, lb.getId(), lb.getAccountId(), lb.getNodes());

        final String[][] enabledNodes = getServiceStubs().getPoolBinding().getNodes(new String[]{poolName()});
        final String[][] disabledNodes = getServiceStubs().getPoolBinding().getDisabledNodes(new String[]{poolName()});
        final String[][] drainingNodes = getServiceStubs().getPoolBinding().getDrainingNodes(new String[]{poolName()});

        final PoolWeightingsDefinition[][] enabledNodeWeights = getServiceStubs().getPoolBinding().getNodesWeightings(new String[]{poolName()}, enabledNodes);
        Assert.assertEquals(1, enabledNodeWeights.length);
        Assert.assertEquals(2, enabledNodeWeights[0].length);
        Assert.assertTrue((enabledNodeWeights[0][0].getWeighting() == node1.getWeight()) || (enabledNodeWeights[0][0].getWeighting() == node2.getWeight()));
        Assert.assertTrue((enabledNodeWeights[0][1].getWeighting() == node1.getWeight()) || (enabledNodeWeights[0][1].getWeighting() == node2.getWeight()));

        final PoolWeightingsDefinition[][] disabledNodeWeights = getServiceStubs().getPoolBinding().getNodesWeightings(new String[]{poolName()}, disabledNodes);
        Assert.assertEquals(1, disabledNodeWeights.length);
        Assert.assertEquals(0, disabledNodeWeights[0].length);

        final PoolWeightingsDefinition[][] drainingNodeWeights = getServiceStubs().getPoolBinding().getNodesWeightings(new String[]{poolName()}, drainingNodes);
        Assert.assertEquals(1, drainingNodeWeights.length);
        Assert.assertEquals(2, drainingNodeWeights[0].length);
        Assert.assertTrue((drainingNodeWeights[0][0].getWeighting() == node1.getWeight()) || (drainingNodeWeights[0][0].getWeighting() == node2.getWeight()));
        Assert.assertTrue((drainingNodeWeights[0][1].getWeighting() == node1.getWeight()) || (drainingNodeWeights[0][1].getWeighting() == node2.getWeight()));
    }

    private void removeNode() throws Exception {
        zxtmAdapter.removeNode(config, lb.getId(), lb.getAccountId(), node2.getIpAddress(), node2.getPort());

        final String[][] enabledNodes = getServiceStubs().getPoolBinding().getNodes(new String[]{poolName()});
        Assert.assertEquals(1, enabledNodes.length);
        Assert.assertEquals(1, enabledNodes[0].length);

        final String[][] disabledNodes = getServiceStubs().getPoolBinding().getDisabledNodes(new String[]{poolName()});
        Assert.assertEquals(1, disabledNodes.length);
        Assert.assertEquals(0, disabledNodes[0].length);

        final String[][] drainingNodes = getServiceStubs().getPoolBinding().getDrainingNodes(new String[]{poolName()});
        Assert.assertEquals(1, drainingNodes.length);
    }
}
