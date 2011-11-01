package org.openstack.atlas.adapter.zxtm;

import com.zxtm.service.client.CatalogMonitorType;
import com.zxtm.service.client.PoolLoadBalancingAlgorithm;
import com.zxtm.service.client.PoolWeightingsDefinition;
import org.apache.axis.types.UnsignedInt;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.adapter.exception.BadRequestException;
import org.openstack.atlas.adapter.exception.RollbackException;
import org.openstack.atlas.adapter.zxtm.helper.IpHelper;
import org.openstack.atlas.datamodel.CoreAlgorithmType;
import org.openstack.atlas.datamodel.CoreHealthMonitorType;
import org.openstack.atlas.datamodel.CorePersistenceType;
import org.openstack.atlas.service.domain.entity.ConnectionThrottle;
import org.openstack.atlas.service.domain.entity.HealthMonitor;
import org.openstack.atlas.service.domain.entity.Node;
import org.openstack.atlas.service.domain.entity.SessionPersistence;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

/*
 * IMPORTANT! PLEASE READ!
 * Order matters when running these tests so please be careful.
 */
public class SimpleITest extends ITestBase {

    @BeforeClass
    public static void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupLb1();
        setupSimpleLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass() {
        removeSimpleLoadBalancer();
    }

    @Test
    public void updateLoadBalancer() throws Exception {
        try {
            lb_1.setAlgorithm(CoreAlgorithmType.LEAST_CONNECTIONS);
            zxtmAdapter.updateLoadBalancer(config, lb_1);

            final PoolLoadBalancingAlgorithm[] algorithms = getServiceStubs().getPoolBinding().getLoadBalancingAlgorithm(new String[]{poolName(lb_1)});
            Assert.assertEquals(1, algorithms.length);
            Assert.assertEquals(PoolLoadBalancingAlgorithm.connections.toString(), algorithms[0].getValue());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testNodeOperations() throws Exception {
        createNodes();
        updateNodeConditionsToEnabled();
        shouldRollbackWhenUpdatingAllNodeConditionsToDisabled();
        shouldRollbackWhenSettingUnsupportedNodeWeight();
        updateNodeWeights();
        removeNode();
    }

    private void createNodes() throws Exception {
        final int defaultNodeWeight = 1;
        Node node3 = new Node();
        Node node4 = new Node();
        node3.setAddress("127.0.0.3");
        node4.setAddress("127.0.0.4");
        node3.setPort(81);
        node4.setPort(82);
        node3.setEnabled(true);
        node4.setEnabled(false);
        node3.setWeight(15);
        node4.setWeight(20);

        lb_1.getNodes().clear();
        lb_1.getNodes().add(node3);
        lb_1.getNodes().add(node4);

        zxtmAdapter.createNodes(config, lb_1.getId(), lb_1.getAccountId(), lb_1.getNodes());

        String node1ZeusString = IpHelper.createZeusIpString(node_1_1.getAddress(), node_1_1.getPort());
        String node2ZeusString = IpHelper.createZeusIpString(node_1_2.getAddress(), node_1_2.getPort());
        String node3ZeusString = IpHelper.createZeusIpString(node3.getAddress(), node3.getPort());
        String node4ZeusString = IpHelper.createZeusIpString(node4.getAddress(), node4.getPort());

        final String[][] enabledNodes = getServiceStubs().getPoolBinding().getNodes(new String[]{poolName(lb_1)});
        Assert.assertEquals(1, enabledNodes.length);
        Assert.assertEquals(2, enabledNodes[0].length);
        Assert.assertEquals(node1ZeusString, enabledNodes[0][0]);
        Assert.assertEquals(node3ZeusString, enabledNodes[0][1]);

        final String[][] disabledNodes = getServiceStubs().getPoolBinding().getDisabledNodes(new String[]{poolName(lb_1)});
        Assert.assertEquals(1, disabledNodes.length);
        Assert.assertEquals(2, disabledNodes[0].length);
        Assert.assertEquals(node2ZeusString, disabledNodes[0][0]);
        Assert.assertEquals(node4ZeusString, disabledNodes[0][1]);

        final PoolWeightingsDefinition[][] weightingsDefinitions = getServiceStubs().getPoolBinding().getWeightings(new String[]{poolName(lb_1)});
        Assert.assertEquals(1, weightingsDefinitions.length);
        Assert.assertEquals(4, weightingsDefinitions[0].length);

        for (PoolWeightingsDefinition weightingsDefinition : weightingsDefinitions[0]) {
            if (weightingsDefinition.getNode().equals(node1ZeusString))
                Assert.assertEquals(defaultNodeWeight, weightingsDefinition.getWeighting());
            else if (weightingsDefinition.getNode().equals(node2ZeusString))
                Assert.assertEquals(defaultNodeWeight, weightingsDefinition.getWeighting());
            else if (weightingsDefinition.getNode().equals(node3ZeusString))
                Assert.assertEquals(node3.getWeight().intValue(), weightingsDefinition.getWeighting());
            else if (weightingsDefinition.getNode().equals(node4ZeusString))
                Assert.assertEquals(node4.getWeight().intValue(), weightingsDefinition.getWeighting());
            else Assert.fail("Unrecognized node weighting definition.");
        }

        // Remove so later tests aren't affected
        Set<Node> nodesToDelete = new HashSet<Node>();
        nodesToDelete.add(node3);
        nodesToDelete.add(node4);
        zxtmAdapter.deleteNodes(config, lb_1.getAccountId(), lb_1.getId(), nodesToDelete);
        lb_1.getNodes().remove(node3);
        lb_1.getNodes().remove(node4);
        // Re-add the original nodes
        lb_1.getNodes().add(node_1_1);
        lb_1.getNodes().add(node_1_2);
    }

    private void updateNodeConditionsToEnabled() throws Exception {
        for (Node node : lb_1.getNodes()) {
            node.setEnabled(true);
            zxtmAdapter.updateNode(config, lb_1.getAccountId(), lb_1.getId(), node);
        }

        assertThatAllNodesAreEnabled();
    }

    private void assertThatAllNodesAreEnabled() throws RemoteException, BadRequestException {
        final String[][] enabledNodes = getServiceStubs().getPoolBinding().getNodes(new String[]{poolName(lb_1)});
        Assert.assertEquals(1, enabledNodes.length);
        Assert.assertEquals(2, enabledNodes[0].length);

        final String[][] disabledNodes = getServiceStubs().getPoolBinding().getDisabledNodes(new String[]{poolName(lb_1)});
        Assert.assertEquals(1, disabledNodes.length);
        Assert.assertEquals(0, disabledNodes[0].length);

        final String[][] drainingNodes = getServiceStubs().getPoolBinding().getDrainingNodes(new String[]{poolName(lb_1)});
        Assert.assertEquals(0, drainingNodes[0].length);
    }

    private void shouldRollbackWhenUpdatingAllNodeConditionsToDisabled() throws Exception {
        try {
            assertThatAllNodesAreEnabled();
            for (Node node : lb_1.getNodes()) {
                node.setEnabled(false);
                zxtmAdapter.updateNode(config, lb_1.getAccountId(), lb_1.getId(), node);
            }
        } catch (Exception e) {
            if (e instanceof RollbackException) updateNodeConditionsToEnabled();
            else Assert.fail("Expected a RollbackException.");
        }
    }

    private void shouldRollbackWhenSettingUnsupportedNodeWeight() throws Exception {
        node_1_1.setWeight(0);

        try {
            zxtmAdapter.updateNode(config, lb_1.getAccountId(), lb_1.getId(), node_1_1);
        } catch (Exception e) {
            if (e instanceof RollbackException) {
                final String[][] enabledNodes = getServiceStubs().getPoolBinding().getNodes(new String[]{poolName(lb_1)});
                final String[][] disabledNodes = getServiceStubs().getPoolBinding().getDisabledNodes(new String[]{poolName(lb_1)});
                final String[][] drainingNodes = getServiceStubs().getPoolBinding().getDrainingNodes(new String[]{poolName(lb_1)});

                final PoolWeightingsDefinition[][] enabledNodeWeights = getServiceStubs().getPoolBinding().getNodesWeightings(new String[]{poolName(lb_1)}, enabledNodes);
                Assert.assertEquals(1, enabledNodeWeights.length);
                Assert.assertEquals(2, enabledNodeWeights[0].length);
                Assert.assertEquals(1, enabledNodeWeights[0][0].getWeighting());
                Assert.assertEquals(1, enabledNodeWeights[0][1].getWeighting());

                final PoolWeightingsDefinition[][] disabledNodeWeights = getServiceStubs().getPoolBinding().getNodesWeightings(new String[]{poolName(lb_1)}, disabledNodes);
                Assert.assertEquals(1, disabledNodeWeights.length);
                Assert.assertEquals(0, disabledNodeWeights[0].length);

                final PoolWeightingsDefinition[][] drainingNodeWeights = getServiceStubs().getPoolBinding().getNodesWeightings(new String[]{poolName(lb_1)}, drainingNodes);
                Assert.assertEquals(1, drainingNodeWeights.length);
                Assert.assertEquals(0, drainingNodeWeights[0].length);
            } else {
                Assert.fail("RollbackException expected.");
            }
        }
    }

    private void updateNodeWeights() throws Exception {
        node_1_1.setWeight(50);
        node_1_2.setWeight(100);

        zxtmAdapter.updateNode(config, lb_1.getAccountId(), lb_1.getId(), node_1_1);
        zxtmAdapter.updateNode(config, lb_1.getAccountId(), lb_1.getId(), node_1_2);

        final String[][] enabledNodes = getServiceStubs().getPoolBinding().getNodes(new String[]{poolName(lb_1)});
        final String[][] disabledNodes = getServiceStubs().getPoolBinding().getDisabledNodes(new String[]{poolName(lb_1)});
        final String[][] drainingNodes = getServiceStubs().getPoolBinding().getDrainingNodes(new String[]{poolName(lb_1)});

        final PoolWeightingsDefinition[][] enabledNodeWeights = getServiceStubs().getPoolBinding().getNodesWeightings(new String[]{poolName(lb_1)}, enabledNodes);
        Assert.assertEquals(1, enabledNodeWeights.length);
        Assert.assertEquals(2, enabledNodeWeights[0].length);
        Assert.assertTrue((enabledNodeWeights[0][0].getWeighting() == node_1_1.getWeight()) || (enabledNodeWeights[0][0].getWeighting() == node_1_2.getWeight()));
        Assert.assertTrue((enabledNodeWeights[0][1].getWeighting() == node_1_1.getWeight()) || (enabledNodeWeights[0][1].getWeighting() == node_1_2.getWeight()));

        final PoolWeightingsDefinition[][] disabledNodeWeights = getServiceStubs().getPoolBinding().getNodesWeightings(new String[]{poolName(lb_1)}, disabledNodes);
        Assert.assertEquals(1, disabledNodeWeights.length);
        Assert.assertEquals(0, disabledNodeWeights[0].length);

        final PoolWeightingsDefinition[][] drainingNodeWeights = getServiceStubs().getPoolBinding().getNodesWeightings(new String[]{poolName(lb_1)}, drainingNodes);
        Assert.assertEquals(1, drainingNodeWeights.length);
        Assert.assertEquals(0, drainingNodeWeights[0].length);
    }

    private void removeNode() throws Exception {
        Set<Node> nodesToDelete = new HashSet<Node>();
        nodesToDelete.add(node_1_2);
        zxtmAdapter.deleteNodes(config, lb_1.getAccountId(), lb_1.getId(), nodesToDelete);

        final String[][] enabledNodes = getServiceStubs().getPoolBinding().getNodes(new String[]{poolName(lb_1)});
        Assert.assertEquals(1, enabledNodes.length);
        Assert.assertEquals(1, enabledNodes[0].length);

        final String[][] disabledNodes = getServiceStubs().getPoolBinding().getDisabledNodes(new String[]{poolName(lb_1)});
        Assert.assertEquals(1, disabledNodes.length);
        Assert.assertEquals(0, disabledNodes[0].length);

        final String[][] drainingNodes = getServiceStubs().getPoolBinding().getDrainingNodes(new String[]{poolName(lb_1)});
        Assert.assertEquals(1, drainingNodes.length);
    }

    @Test
    public void testAllSessionPersistenceOperations() throws Exception {
        updateSessionPersistence();
        deleteSessionPersistence();
    }

    private void updateSessionPersistence() throws Exception {
        SessionPersistence persistence = new SessionPersistence();
        zxtmAdapter.setSessionPersistence(config, lb_1.getId(), lb_1.getAccountId(), persistence);

        final String[] persistenceNamesForPools = getServiceStubs().getPoolBinding().getPersistence(new String[]{poolName(lb_1)});
        Assert.assertEquals(1, persistenceNamesForPools.length);
        Assert.assertEquals(CorePersistenceType.HTTP_COOKIE, persistenceNamesForPools[0]);

        final String[] allPersistenceClasses = getServiceStubs().getPersistenceBinding().getPersistenceNames();
        boolean doesPersistenceClassExist = false;

        for (String persistenceClass : allPersistenceClasses) {
            if (persistenceClass.equals(persistenceNamesForPools[0])) {
                doesPersistenceClassExist = true;
                break;
            }
        }

        Assert.assertTrue(doesPersistenceClassExist);
    }

    private void deleteSessionPersistence() throws Exception {
        final String[] persistenceNamesForPools = getServiceStubs().getPoolBinding().getPersistence(new String[]{poolName(lb_1)});
        Assert.assertEquals(CorePersistenceType.HTTP_COOKIE, persistenceNamesForPools[0]);

        try {
            zxtmAdapter.deleteSessionPersistence(config, lb_1.getAccountId(), lb_1.getId());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        final String[] deletedPersistenceNamesForPools = getServiceStubs().getPoolBinding().getPersistence(new String[]{poolName(lb_1)});
        Assert.assertEquals(1, deletedPersistenceNamesForPools.length);
        Assert.assertEquals("", deletedPersistenceNamesForPools[0]);

        final String[] allPersistenceClasses = getServiceStubs().getPersistenceBinding().getPersistenceNames();
        boolean doesPersistenceClassExist = false;

        for (String persistenceClass : allPersistenceClasses) {
            if (persistenceClass.equals(persistenceNamesForPools[0])) {
                doesPersistenceClassExist = true;
                break;
            }
        }

        Assert.assertTrue(doesPersistenceClassExist);
    }

    @Test
    public void updateConnectionThrottle() throws Exception {
        ConnectionThrottle throttle = new ConnectionThrottle();
        throttle.setMaxRequestRate(2000);
        throttle.setRateInterval(60);
        zxtmAdapter.updateConnectionThrottle(config, lb_1.getId(), lb_1.getAccountId(), throttle);

        final UnsignedInt[] maxConnectionRates = getServiceStubs().getProtectionBinding().getMaxConnectionRate(new String[]{protectionClassName(lb_1)});
        Assert.assertEquals(1, maxConnectionRates.length);
        Assert.assertEquals(throttle.getMaxRequestRate().intValue(), maxConnectionRates[0].intValue());

        final UnsignedInt[] rateIntervals = getServiceStubs().getProtectionBinding().getRateTimer(new String[]{protectionClassName(lb_1)});
        Assert.assertEquals(1, rateIntervals.length);
        Assert.assertEquals(throttle.getRateInterval().intValue(), rateIntervals[0].intValue());
    }

    @Test
    public void testHealthMonitorOperations() throws Exception {
        updateHttpHealthMonitor();
        deleteHttpHealthMonitor();
        updateHttpsHealthMonitor();
        removeHttpsHealthMonitor();
    }

    private void updateHttpHealthMonitor() throws Exception {
        HealthMonitor monitor = new HealthMonitor();
        monitor.setType(CoreHealthMonitorType.HTTP);
        monitor.setAttemptsBeforeDeactivation(10);
        monitor.setPath("/");
        monitor.setDelay(60);
        monitor.setTimeout(90);

        zxtmAdapter.updateHealthMonitor(config, lb_1.getId(), lb_1.getAccountId(), monitor);

        String monitorName = monitorName(lb_1);

        final CatalogMonitorType[] monitorTypeArray = getServiceStubs().getMonitorBinding().getType(new String[]{monitorName});
        Assert.assertEquals(1, monitorTypeArray.length);
        Assert.assertEquals(CatalogMonitorType._http, monitorTypeArray[0].getValue());

        final UnsignedInt[] attemptsBeforeDeactivationArray = getServiceStubs().getMonitorBinding().getFailures(new String[]{monitorName});
        Assert.assertEquals(1, attemptsBeforeDeactivationArray.length);
        Assert.assertEquals(new UnsignedInt(monitor.getAttemptsBeforeDeactivation()), attemptsBeforeDeactivationArray[0]);

        final String[] pathRegexArray = getServiceStubs().getMonitorBinding().getPath(new String[]{monitorName});
        Assert.assertEquals(1, pathRegexArray.length);
        Assert.assertEquals(monitor.getPath(), pathRegexArray[0]);

        final UnsignedInt[] delayArray = getServiceStubs().getMonitorBinding().getDelay(new String[]{monitorName});
        Assert.assertEquals(1, delayArray.length);
        Assert.assertEquals(new UnsignedInt(monitor.getDelay()), delayArray[0]);

        final UnsignedInt[] timeoutArray = getServiceStubs().getMonitorBinding().getTimeout(new String[]{monitorName});
        Assert.assertEquals(1, timeoutArray.length);
        Assert.assertEquals(new UnsignedInt(monitor.getTimeout()), timeoutArray[0]);

        final boolean[] useSslArray = getServiceStubs().getMonitorBinding().getUseSSL(new String[]{monitorName});
        Assert.assertEquals(1, useSslArray.length);
        Assert.assertFalse(useSslArray[0]);
    }

    private void deleteHttpHealthMonitor() throws Exception {
        zxtmAdapter.deleteHealthMonitor(config, lb_1.getAccountId(), lb_1.getId());

        String monitorName = monitorName(lb_1);
        String[] allMonitorNames = getServiceStubs().getMonitorBinding().getAllMonitorNames();

        for (String someMonitorName : allMonitorNames) {
            if (someMonitorName.equals(monitorName)) Assert.fail("Monitor should not exist.");
        }
    }

    private void updateHttpsHealthMonitor() throws Exception {
        HealthMonitor monitor = new HealthMonitor();
        monitor.setType(CoreHealthMonitorType.HTTPS);
        monitor.setAttemptsBeforeDeactivation(10);
        monitor.setPath("/");
        monitor.setDelay(60);
        monitor.setTimeout(90);

        zxtmAdapter.updateHealthMonitor(config, lb_1.getAccountId(), lb_1.getId(), monitor);

        String monitorName = monitorName(lb_1);

        final CatalogMonitorType[] monitorTypeArray = getServiceStubs().getMonitorBinding().getType(new String[]{monitorName});
        Assert.assertEquals(1, monitorTypeArray.length);
        Assert.assertEquals(CatalogMonitorType._http, monitorTypeArray[0].getValue());

        final UnsignedInt[] attemptsBeforeDeactivationArray = getServiceStubs().getMonitorBinding().getFailures(new String[]{monitorName});
        Assert.assertEquals(1, attemptsBeforeDeactivationArray.length);
        Assert.assertEquals(new UnsignedInt(monitor.getAttemptsBeforeDeactivation()), attemptsBeforeDeactivationArray[0]);

        final String[] pathRegexArray = getServiceStubs().getMonitorBinding().getPath(new String[]{monitorName});
        Assert.assertEquals(1, pathRegexArray.length);
        Assert.assertEquals(monitor.getPath(), pathRegexArray[0]);

        final UnsignedInt[] delayArray = getServiceStubs().getMonitorBinding().getDelay(new String[]{monitorName});
        Assert.assertEquals(1, delayArray.length);
        Assert.assertEquals(new UnsignedInt(monitor.getDelay()), delayArray[0]);

        final UnsignedInt[] timeoutArray = getServiceStubs().getMonitorBinding().getTimeout(new String[]{monitorName});
        Assert.assertEquals(1, timeoutArray.length);
        Assert.assertEquals(new UnsignedInt(monitor.getTimeout()), timeoutArray[0]);

        final boolean[] useSslArray = getServiceStubs().getMonitorBinding().getUseSSL(new String[]{monitorName});
        Assert.assertEquals(1, useSslArray.length);
        Assert.assertTrue(useSslArray[0]);
    }

    private void removeHttpsHealthMonitor() throws Exception {
        zxtmAdapter.deleteHealthMonitor(config, lb_1.getAccountId(), lb_1.getId());

        String monitorName = monitorName(lb_1);
        String[] allMonitorNames = getServiceStubs().getMonitorBinding().getAllMonitorNames();

        for (String someMonitorName : allMonitorNames) {
            if (someMonitorName.equals(monitorName)) Assert.fail("Monitor should not exist.");
        }
    }
}
