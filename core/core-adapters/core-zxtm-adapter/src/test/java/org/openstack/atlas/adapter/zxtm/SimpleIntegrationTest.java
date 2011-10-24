package org.openstack.atlas.adapter.zxtm;

import com.zxtm.service.client.*;
import org.apache.axis.types.UnsignedInt;
import org.junit.*;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.adapter.exception.BadRequestException;
import org.openstack.atlas.adapter.exception.RollbackException;
import org.openstack.atlas.adapter.zxtm.helper.IpHelper;
import org.openstack.atlas.datamodel.CoreAlgorithmType;
import org.openstack.atlas.datamodel.CoreHealthMonitorType;
import org.openstack.atlas.datamodel.CorePersistenceType;
import org.openstack.atlas.datamodel.CoreProtocolType;
import org.openstack.atlas.service.domain.entity.ConnectionThrottle;
import org.openstack.atlas.service.domain.entity.HealthMonitor;
import org.openstack.atlas.service.domain.entity.Node;
import org.openstack.atlas.service.domain.entity.SessionPersistence;

import java.rmi.RemoteException;

/*
 * IMPORTANT! PLEASE READ!
 * Order matters when running this test so please be careful.
 */
@Ignore // Run manually
public class SimpleIntegrationTest extends ZeusTestBase {

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

/*    @Test
    public void updateProtocol() {
        try {
            zxtmAdapter.updateProtocol(config, lb.getId(), lb.getAccountId(), CoreProtocolType.HTTPS);

            final VirtualServerBasicInfo[] virtualServerBasicInfos = getServiceStubs().getVirtualServerBinding().getBasicInfo(new String[]{loadBalancerName()});
            Assert.assertEquals(1, virtualServerBasicInfos.length);
            Assert.assertEquals(VirtualServerProtocol.https, virtualServerBasicInfos[0].getProtocol());

            final VirtualServerRule[][] virtualServerRules = getServiceStubs().getVirtualServerBinding().getRules(new String[]{loadBalancerName()});
            Assert.assertEquals(1, virtualServerRules.length);

            for (VirtualServerRule virtualServerRule : virtualServerRules[0]) {
                if (virtualServerRule.equals(ZxtmAdapterImpl.ruleXForwardedFor))
                    Assert.fail("XFF rule should not be enabled!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void updatePort() throws Exception {
        try {
            zxtmAdapter.updatePort(config, lb.getId(), lb.getAccountId(), 8080);

            final VirtualServerBasicInfo[] virtualServerBasicInfos = getServiceStubs().getVirtualServerBinding().getBasicInfo(new String[]{loadBalancerName()});
            Assert.assertEquals(1, virtualServerBasicInfos.length);
            Assert.assertEquals(8080, virtualServerBasicInfos[0].getPort());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void updateAlgorithm() throws Exception {
        try {
            zxtmAdapter.setLoadBalancingAlgorithm(config, lb.getId(), lb.getAccountId(), CoreAlgorithmType.ROUND_ROBIN);

            final PoolLoadBalancingAlgorithm[] algorithms = getServiceStubs().getPoolBinding().getLoadBalancingAlgorithm(new String[]{poolName()});
            Assert.assertEquals(1, algorithms.length);
            Assert.assertEquals(PoolLoadBalancingAlgorithm.random.toString(), algorithms[0].getValue());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testNodeOperations() throws Exception {
        setNodes();
        updateNodeConditionsToEnabled();
        shouldRollbackWhenUpdatingAllNodeConditionsToDisabled();
        shouldRollbackWhenSettingUnsupportedNodeWeights();
        updateNodeWeights();
        removeNode();
    }

    private void setNodes() throws Exception {
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

        lb.getNodes().add(node3);
        lb.getNodes().add(node4);

        zxtmAdapter.setNodes(config, lb.getId(), lb.getAccountId(), lb.getNodes());

        String node1ZeusString = IpHelper.createZeusIpString(node1.getAddress(), node1.getPort());
        String node2ZeusString = IpHelper.createZeusIpString(node2.getAddress(), node2.getPort());
        String node3ZeusString = IpHelper.createZeusIpString(node3.getAddress(), node3.getPort());
        String node4ZeusString = IpHelper.createZeusIpString(node4.getAddress(), node4.getPort());

        final String[][] enabledNodes = getServiceStubs().getPoolBinding().getNodes(new String[]{poolName()});
        Assert.assertEquals(1, enabledNodes.length);
        Assert.assertEquals(2, enabledNodes[0].length);
        Assert.assertEquals(node1ZeusString, enabledNodes[0][0]);
        Assert.assertEquals(node3ZeusString, enabledNodes[0][1]);

        final String[][] disabledNodes = getServiceStubs().getPoolBinding().getDisabledNodes(new String[]{poolName()});
        Assert.assertEquals(1, disabledNodes.length);
        Assert.assertEquals(2, disabledNodes[0].length);
        Assert.assertEquals(node2ZeusString, disabledNodes[0][0]);
        Assert.assertEquals(node4ZeusString, disabledNodes[0][1]);

        final PoolWeightingsDefinition[][] weightingsDefinitions = getServiceStubs().getPoolBinding().getWeightings(new String[]{poolName()});
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
        lb.getNodes().remove(node3);
        lb.getNodes().remove(node4);
        zxtmAdapter.setNodes(config, lb.getId(), lb.getAccountId(), lb.getNodes());
    }

    private void updateNodeConditionsToEnabled() throws Exception {
        for (Node node : lb.getNodes()) {
            node.setEnabled(true);
        }

        zxtmAdapter.setNodes(config, lb.getId(), lb.getAccountId(), lb.getNodes());

        assertThatAllNodesAreEnabled();
    }

    private void assertThatAllNodesAreEnabled() throws RemoteException, BadRequestException {
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
            node.setEnabled(false);
        }

        try {
            assertThatAllNodesAreEnabled();
            zxtmAdapter.setNodes(config, lb.getId(), lb.getAccountId(), lb.getNodes());
        } catch (Exception e) {
            if (e instanceof RollbackException) assertThatAllNodesAreEnabled();
            else Assert.fail("Expected a RollbackException.");
        }
    }

    private void shouldRollbackWhenSettingUnsupportedNodeWeights() throws Exception {
        node1.setWeight(0);
        node2.setWeight(101);

        try {
            zxtmAdapter.setNodeWeights(config, lb.getId(), lb.getAccountId(), lb.getNodes());
        } catch (Exception e) {
            if (e instanceof RollbackException) {
                final String[][] enabledNodes = getServiceStubs().getPoolBinding().getNodes(new String[]{poolName()});
                final String[][] disabledNodes = getServiceStubs().getPoolBinding().getDisabledNodes(new String[]{poolName()});
                final String[][] drainingNodes = getServiceStubs().getPoolBinding().getDrainingNodes(new String[]{poolName()});

                final PoolWeightingsDefinition[][] enabledNodeWeights = getServiceStubs().getPoolBinding().getNodesWeightings(new String[]{poolName()}, enabledNodes);
                Assert.assertEquals(1, enabledNodeWeights.length);
                Assert.assertEquals(2, enabledNodeWeights[0].length);
                Assert.assertEquals(1, enabledNodeWeights[0][0].getWeighting());
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
                Assert.fail("RollbackException expected.");
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
        zxtmAdapter.removeNode(config, lb.getId(), lb.getAccountId(), node2.getAddress(), node2.getPort());

        final String[][] enabledNodes = getServiceStubs().getPoolBinding().getNodes(new String[]{poolName()});
        Assert.assertEquals(1, enabledNodes.length);
        Assert.assertEquals(1, enabledNodes[0].length);

        final String[][] disabledNodes = getServiceStubs().getPoolBinding().getDisabledNodes(new String[]{poolName()});
        Assert.assertEquals(1, disabledNodes.length);
        Assert.assertEquals(0, disabledNodes[0].length);

        final String[][] drainingNodes = getServiceStubs().getPoolBinding().getDrainingNodes(new String[]{poolName()});
        Assert.assertEquals(1, drainingNodes.length);
    }*/

    @Test
    public void testAllSessionPersistenceOperations() throws Exception {
        updateSessionPersistence();
        deleteSessionPersistence();
//        shouldDisableSessionPersistenceWhenUpdatingToNonHttpProtocol();
    }

    private void updateSessionPersistence() throws Exception {
        SessionPersistence persistence = new SessionPersistence();
        zxtmAdapter.setSessionPersistence(config, lb.getId(), lb.getAccountId(), persistence);

        final String[] persistenceNamesForPools = getServiceStubs().getPoolBinding().getPersistence(new String[]{poolName()});
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
        final String[] persistenceNamesForPools = getServiceStubs().getPoolBinding().getPersistence(new String[]{poolName()});
        Assert.assertEquals(CorePersistenceType.HTTP_COOKIE, persistenceNamesForPools[0]);

        try {
            zxtmAdapter.deleteSessionPersistence(config, lb.getAccountId(), lb.getId());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        final String[] deletedPersistenceNamesForPools = getServiceStubs().getPoolBinding().getPersistence(new String[]{poolName()});
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

/*    private void shouldDisableSessionPersistenceWhenUpdatingToNonHttpProtocol() throws AdapterException, RemoteException {
        SessionPersistence persistence = new SessionPersistence();
        zxtmAdapter.setSessionPersistence(config, lb.getId(), lb.getAccountId(), persistence);
        String[] persistenceCatalogList = getServiceStubs().getPoolBinding().getPersistence(new String[]{poolName()});
        Assert.assertEquals(1, persistenceCatalogList.length);
        Assert.assertEquals(CorePersistenceType.HTTP_COOKIE, persistenceCatalogList[0]);

        zxtmAdapter.updateProtocol(config, lb.getId(), lb.getAccountId(), CoreProtocolType.HTTPS);
        persistenceCatalogList = getServiceStubs().getPoolBinding().getPersistence(new String[]{poolName()});
        Assert.assertEquals(1, persistenceCatalogList.length);
        Assert.assertEquals("", persistenceCatalogList[0]);
    }*/

    @Test
    public void updateConnectionThrottle() throws Exception {
        ConnectionThrottle throttle = new ConnectionThrottle();
        throttle.setMaxRequestRate(2000);
        throttle.setRateInterval(60);
        zxtmAdapter.updateConnectionThrottle(config, lb.getId(), lb.getAccountId(), throttle);

        final UnsignedInt[] maxConnectionRates = getServiceStubs().getProtectionBinding().getMaxConnectionRate(new String[]{protectionClassName()});
        Assert.assertEquals(1, maxConnectionRates.length);
        Assert.assertEquals(throttle.getMaxRequestRate().intValue(), maxConnectionRates[0].intValue());

        final UnsignedInt[] rateIntervals = getServiceStubs().getProtectionBinding().getRateTimer(new String[]{protectionClassName()});
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

        zxtmAdapter.updateHealthMonitor(config, lb.getId(), lb.getAccountId(), monitor);

        String monitorName = monitorName();

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
        zxtmAdapter.deleteHealthMonitor(config, lb.getAccountId(), lb.getId());

        String monitorName = monitorName();
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

        zxtmAdapter.updateHealthMonitor(config, lb.getAccountId(), lb.getId(), monitor);

        String monitorName = monitorName();

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
        zxtmAdapter.deleteHealthMonitor(config, lb.getAccountId(), lb.getId());

        String monitorName = monitorName();
        String[] allMonitorNames = getServiceStubs().getMonitorBinding().getAllMonitorNames();

        for (String someMonitorName : allMonitorNames) {
            if (someMonitorName.equals(monitorName)) Assert.fail("Monitor should not exist.");
        }
    }
}
