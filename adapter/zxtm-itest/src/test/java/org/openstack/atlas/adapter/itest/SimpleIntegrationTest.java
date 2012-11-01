package org.openstack.atlas.adapter.itest;

import com.zxtm.service.client.*;
import org.apache.axis.types.UnsignedInt;
import org.junit.*;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.ZxtmRollBackException;
import org.openstack.atlas.adapter.helpers.IpHelper;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.adapter.zxtm.ZxtmAdapterImpl;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.util.ip.IPv6;

import java.rmi.RemoteException;
import java.util.*;

import static org.openstack.atlas.service.domain.entities.AccessListType.ALLOW;
import static org.openstack.atlas.service.domain.entities.AccessListType.DENY;
import static org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm.RANDOM;
import static org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm.WEIGHTED_LEAST_CONNECTIONS;
import static org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.*;
import static org.openstack.atlas.service.domain.entities.NodeCondition.*;
import static org.openstack.atlas.service.domain.entities.SessionPersistence.HTTP_COOKIE;
import static org.openstack.atlas.service.domain.entities.SessionPersistence.SOURCE_IP;

/*
 * IMPORTANT! PLEASE READ!
 * Order matters when running this test so please be careful.
 */
public class SimpleIntegrationTest extends ZeusTestBase {

    @BeforeClass
    public static void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        setupSimpleLoadBalancer();
    }

    @Before
    public void setUp() {
        setupIvars();
    }

    @AfterClass
    public static void tearDownClass() {
        removeSimpleLoadBalancer();
    }

    @Test
    public void updateProtocol() {
        try {
            lb.setProtocol(HTTPS);
            zxtmAdapter.updateProtocol(config, lb);

            final VirtualServerBasicInfo[] virtualServerBasicInfos = getServiceStubs().getVirtualServerBinding().getBasicInfo(new String[]{loadBalancerName()});
            Assert.assertEquals(1, virtualServerBasicInfos.length);
            Assert.assertEquals(VirtualServerProtocol.https, virtualServerBasicInfos[0].getProtocol());

            final VirtualServerRule[][] virtualServerRules = getServiceStubs().getVirtualServerBinding().getRules(new String[]{loadBalancerName()});
            Assert.assertEquals(1, virtualServerRules.length);

            for (VirtualServerRule virtualServerRule : virtualServerRules[0]) {
                if (virtualServerRule.equals(ZxtmAdapterImpl.ruleXForwardedFor) || virtualServerRule.equals(ZxtmAdapterImpl.ruleXForwardedProto))
                    Assert.fail("XFF and XFP rule should not be enabled!");
            }

            lb.setProtocol(HTTP);
            zxtmAdapter.updateProtocol(config, lb);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void updateTimeout() {
        try {
            UnsignedInt[] mrt1 = getServiceStubs().getPoolBinding().getMaxReplyTime(new String[]{poolName()});
            Assert.assertEquals(new UnsignedInt(30), mrt1[0]);

            lb.setTimeout(50);
            zxtmAdapter.updateTimeout(config, lb);

            UnsignedInt[] mrt = getServiceStubs().getPoolBinding().getMaxReplyTime(new String[]{poolName()});
            Assert.assertEquals(new UnsignedInt(50), mrt[0]);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void updateProtocolVerifyXFHeaders() {
        try {
            lb.setProtocol(HTTPS);
            zxtmAdapter.updateProtocol(config, lb);

            final VirtualServerBasicInfo[] virtualServerBasicInfos = getServiceStubs().getVirtualServerBinding().getBasicInfo(new String[]{loadBalancerName()});
            Assert.assertEquals(1, virtualServerBasicInfos.length);
            Assert.assertEquals(VirtualServerProtocol.https, virtualServerBasicInfos[0].getProtocol());

            final VirtualServerRule[][] virtualServerRules = getServiceStubs().getVirtualServerBinding().getRules(new String[]{loadBalancerName()});
            Assert.assertEquals(1, virtualServerRules.length);

            for (VirtualServerRule virtualServerRule : virtualServerRules[0]) {
                if (virtualServerRule.equals(ZxtmAdapterImpl.ruleXForwardedFor) || virtualServerRule.equals(ZxtmAdapterImpl.ruleXForwardedProto))
                    Assert.fail("XFF and XFP rule should not be enabled!");
            }

            lb.setProtocol(HTTP);
            zxtmAdapter.updateProtocol(config, lb);

            for (VirtualServerRule virtualServerRule : virtualServerRules[0]) {
                //Both XFF and XFP should be set when updating from any protocol to a HTTP protocol
                Assert.assertTrue(virtualServerRule.equals(ZxtmAdapterImpl.ruleXForwardedFor) || virtualServerRule.equals(ZxtmAdapterImpl.ruleXForwardedProto));
            }

            final VirtualServerRule[][] virtualServerRules2 = getServiceStubs().getVirtualServerBinding().getRules(new String[]{loadBalancerName()});
            Assert.assertEquals(2, virtualServerRules2[0].length);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }


    @Test
    public void changeProtocolWithConnectionLoggingEnabled() {
        try {
            lb.setConnectionLogging(Boolean.TRUE);
            zxtmAdapter.updateConnectionLogging(config, lb);
            lb.setProtocol(HTTPS);
            zxtmAdapter.updateProtocol(config, lb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void changeProtocolWithContentCachingEnabled() {
        try {
            lb.setProtocol(HTTP);
            zxtmAdapter.updateProtocol(config, lb);

            lb.setContentCaching(Boolean.TRUE);
            zxtmAdapter.updateContentCaching(config, lb);
            boolean[] isCCenabled1 = getServiceStubs().getVirtualServerBinding().getWebcacheEnabled(new String[]{loadBalancerName()});
            Assert.assertEquals(Boolean.TRUE, isCCenabled1[0]);

            lb.setProtocol(TCP);
            zxtmAdapter.updateProtocol(config, lb);
            boolean[] isCCenabled2 = getServiceStubs().getVirtualServerBinding().getWebcacheEnabled(new String[]{loadBalancerName()});
            Assert.assertEquals(Boolean.FALSE, isCCenabled2[0]);

        } catch (Exception e) {
            e.printStackTrace();
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
            zxtmAdapter.setLoadBalancingAlgorithm(config, lb.getId(), lb.getAccountId(), RANDOM);

            final PoolLoadBalancingAlgorithm[] algorithms = getServiceStubs().getPoolBinding().getLoadBalancingAlgorithm(new String[]{poolName()});
            Assert.assertEquals(1, algorithms.length);
            Assert.assertEquals(PoolLoadBalancingAlgorithm.random.toString(), algorithms[0].getValue());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testSimpleNodeOperations() throws Exception {
        // Update algorithm so we can test that node weights get set properly
        zxtmAdapter.setLoadBalancingAlgorithm(config, lb.getId(), lb.getAccountId(), WEIGHTED_LEAST_CONNECTIONS);

        setNodes();
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

    @Test
    public void testAllVirtualIpOperations() throws Exception {
        addVirtualIp();
        deleteVirtualIp();
        addIPv6VirtualIp();
        deleteIpv6VirtualIp();
    }

    private void addVirtualIp() throws Exception {
        VirtualIp vip2 = new VirtualIp();
        vip2.setId(ADDITIONAL_VIP_ID);
        vip2.setIpAddress("10.69.0.58");
        LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip();
        loadBalancerJoinVip.setVirtualIp(vip2);
        lb.getLoadBalancerJoinVipSet().add(loadBalancerJoinVip);
        zxtmAdapter.addVirtualIps(config, lb);

        String trafficIpGroupName = trafficIpGroupName(vip2);

        final String[][] vips = getServiceStubs().getTrafficIpGroupBinding().getIPAddresses(new String[]{trafficIpGroupName});
        Assert.assertEquals(1, vips.length);
        Assert.assertEquals(1, vips[0].length);
        Assert.assertEquals(vip2.getIpAddress(), vips[0][0]);
    }

    private void deleteVirtualIp() throws Exception {
        VirtualIp vip = new VirtualIp();
        vip.setId(ADDITIONAL_VIP_ID);

        try {
            zxtmAdapter.deleteVirtualIp(config, lb, vip.getId());
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
            } else Assert.fail(e.getMessage());
        }

        String trafficIpGroupName = ZxtmNameBuilder.generateTrafficIpGroupName(lb, vip.getId());

        try {
            getServiceStubs().getTrafficIpGroupBinding().getIPAddresses(new String[]{trafficIpGroupName});
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
            } else Assert.fail(e.getMessage());
        }
    }

    private void addIPv6VirtualIp() throws Exception {
        VirtualIpv6 ipv6Vip = new VirtualIpv6();
        ipv6Vip.setId(ADDITIONAL_IPV6_VIP_ID);
        ipv6Vip.setAccountId(TEST_ACCOUNT_ID);
        ipv6Vip.setCluster(cluster);
        ipv6Vip.setVipOctets(1);
        LoadBalancerJoinVip6 loadBalancerJoinVip6 = new LoadBalancerJoinVip6();
        loadBalancerJoinVip6.setVirtualIp(ipv6Vip);
        lb.getLoadBalancerJoinVip6Set().add(loadBalancerJoinVip6);
        zxtmAdapter.addVirtualIps(config, lb);

        String trafficIpGroupName = trafficIpGroupName(ipv6Vip);

        final String[][] vips = getServiceStubs().getTrafficIpGroupBinding().getIPAddresses(new String[]{trafficIpGroupName});
        Assert.assertEquals(1, vips.length);
        Assert.assertEquals(1, vips[0].length);
        Assert.assertEquals(new IPv6(ipv6Vip.getDerivedIpString()).expand(), new IPv6(vips[0][0]).expand());
    }

    private void deleteIpv6VirtualIp() throws Exception {
        VirtualIp vip = new VirtualIp();
        vip.setId(ADDITIONAL_IPV6_VIP_ID);

        try {
            zxtmAdapter.deleteVirtualIp(config, lb, vip.getId());
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
            } else Assert.fail(e.getMessage());
        }

        String trafficIpGroupName = ZxtmNameBuilder.generateTrafficIpGroupName(lb, vip.getId());

        try {
            getServiceStubs().getTrafficIpGroupBinding().getIPAddresses(new String[]{trafficIpGroupName});
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
            } else Assert.fail(e.getMessage());
        }
    }

    @Test
    public void setSuspendedLb() throws Exception {
        VirtualIp vip = new VirtualIp();
        vip.setId(ADDITIONAL_IPV6_VIP_ID);

        try {
            zxtmAdapter.suspendLoadBalancer(config, lb);
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
            } else Assert.fail(e.getMessage());
        }

        String trafficIpGroupName = ZxtmNameBuilder.generateTrafficIpGroupName(lb, vip.getId());

        try {
            Assert.assertFalse(getServiceStubs().getTrafficIpGroupBinding().getEnabled(new String[]{trafficIpGroupName})[0]);
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
            } else Assert.fail(e.getMessage());
        }
    }

    @Test
    public void removeSuspendedLb() throws Exception {
        VirtualIp vip = new VirtualIp();
        vip.setId(ADDITIONAL_IPV6_VIP_ID);

        try {
            zxtmAdapter.removeSuspension(config, lb);
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
            } else Assert.fail(e.getMessage());
        }

        String trafficIpGroupName = ZxtmNameBuilder.generateTrafficIpGroupName(lb, vip.getId());

        try {
            Assert.assertTrue(getServiceStubs().getTrafficIpGroupBinding().getEnabled(new String[]{trafficIpGroupName})[0]);
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
            } else Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testAllSessionPersistenceOperations() throws Exception {
        updateSessionPersistenceToCookie();
        removeSessionPersistenceForCookie();
        updateSessionPersistenceToSourceIp();
        removeSessionPersistenceForSourceIp();
        shouldDisableSessionPersistenceWhenUpdatingToNonHttpProtocol();
        shouldDisableSessionPersistenceWhenUpdatingToHttpProtocol();
    }

    private void updateSessionPersistenceToCookie() throws Exception {
        zxtmAdapter.setSessionPersistence(config, lb.getId(), lb.getAccountId(), HTTP_COOKIE);

        final String[] persistenceNamesForPools = getServiceStubs().getPoolBinding().getPersistence(new String[]{poolName()});
        Assert.assertEquals(1, persistenceNamesForPools.length);
        Assert.assertEquals(HTTP_COOKIE.name(), persistenceNamesForPools[0]);

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

    private void updateSessionPersistenceToSourceIp() throws Exception {
        zxtmAdapter.setSessionPersistence(config, lb.getId(), lb.getAccountId(), SOURCE_IP);

        final String[] persistenceNamesForPools = getServiceStubs().getPoolBinding().getPersistence(new String[]{poolName()});
        Assert.assertEquals(1, persistenceNamesForPools.length);
        Assert.assertEquals(SOURCE_IP.name(), persistenceNamesForPools[0]);

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

    private void removeSessionPersistenceForCookie() throws Exception {
        final String[] persistenceNamesForPools = getServiceStubs().getPoolBinding().getPersistence(new String[]{poolName()});
        Assert.assertEquals(HTTP_COOKIE.name(), persistenceNamesForPools[0]);

        try {
            zxtmAdapter.removeSessionPersistence(config, lb.getId(), lb.getAccountId());
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

    private void removeSessionPersistenceForSourceIp() throws Exception {
        final String[] persistenceNamesForPools = getServiceStubs().getPoolBinding().getPersistence(new String[]{poolName()});
        Assert.assertEquals(SOURCE_IP.name(), persistenceNamesForPools[0]);

        try {
            zxtmAdapter.removeSessionPersistence(config, lb.getId(), lb.getAccountId());
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

    private void shouldDisableSessionPersistenceWhenUpdatingToNonHttpProtocol() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
        ZeusTestBase.setupIvars();
        zxtmAdapter.setSessionPersistence(config, lb.getId(), lb.getAccountId(), HTTP_COOKIE);
        String[] persistenceCatalogList = getServiceStubs().getPoolBinding().getPersistence(new String[]{poolName()});
        Assert.assertEquals(1, persistenceCatalogList.length);
        Assert.assertEquals(HTTP_COOKIE.name(), persistenceCatalogList[0]);
        ZeusTestBase.setupIvars();
        lb.setProtocol(TCP);
        zxtmAdapter.updateProtocol(config, lb);
        persistenceCatalogList = getServiceStubs().getPoolBinding().getPersistence(new String[]{poolName()});
        Assert.assertEquals(1, persistenceCatalogList.length);
        Assert.assertEquals("", persistenceCatalogList[0]);
    }

    private void shouldDisableSessionPersistenceWhenUpdatingToHttpProtocol() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
        ZeusTestBase.setupIvars();
        zxtmAdapter.setSessionPersistence(config, lb.getId(), lb.getAccountId(), SOURCE_IP);
        String[] persistenceCatalogList = getServiceStubs().getPoolBinding().getPersistence(new String[]{poolName()});
        Assert.assertEquals(1, persistenceCatalogList.length);
        Assert.assertEquals(SOURCE_IP.name(), persistenceCatalogList[0]);
        ZeusTestBase.setupIvars();
        lb.setProtocol(HTTP);
        zxtmAdapter.updateProtocol(config, lb);
        persistenceCatalogList = getServiceStubs().getPoolBinding().getPersistence(new String[]{poolName()});
        Assert.assertEquals(1, persistenceCatalogList.length);
        Assert.assertEquals("", persistenceCatalogList[0]);
    }

    @Test
    public void updateConnectionThrottle() throws Exception {
        ConnectionLimit throttle = new ConnectionLimit();
        throttle.setMinConnections(1000);
        throttle.setMaxConnections(30);
        throttle.setMaxConnectionRate(2000);
        throttle.setRateInterval(60);
        lb.setConnectionLimit(throttle);
        zxtmAdapter.updateConnectionThrottle(config, lb);

        final UnsignedInt[] minConnections = getServiceStubs().getProtectionBinding().getMinConnections(new String[]{protectionClassName()});
        Assert.assertEquals(1, minConnections.length);
        Assert.assertEquals(throttle.getMinConnections().intValue(), minConnections[0].intValue());

        final UnsignedInt[] maxConnections = getServiceStubs().getProtectionBinding().getMax1Connections(new String[]{protectionClassName()});
        Assert.assertEquals(1, maxConnections.length);
        Assert.assertEquals(throttle.getMaxConnections().intValue(), maxConnections[0].intValue());

        final UnsignedInt[] maxConnectionRates = getServiceStubs().getProtectionBinding().getMaxConnectionRate(new String[]{protectionClassName()});
        Assert.assertEquals(1, maxConnectionRates.length);
        Assert.assertEquals(throttle.getMaxConnectionRate().intValue(), maxConnectionRates[0].intValue());

        final UnsignedInt[] rateIntervals = getServiceStubs().getProtectionBinding().getRateTimer(new String[]{protectionClassName()});
        Assert.assertEquals(1, rateIntervals.length);
        Assert.assertEquals(throttle.getRateInterval().intValue(), rateIntervals[0].intValue());
    }

    @Test
    public void updateAccessList() throws Exception {
        Set<AccessList> networkItems = new HashSet<AccessList>();
        AccessList item1 = new AccessList();
        AccessList item2 = new AccessList();
        item1.setIpAddress("0.0.0.0/0");
        item2.setIpAddress("127.0.0.1");
        item1.setType(DENY);
        item2.setType(ALLOW);
        networkItems.add(item1);
        networkItems.add(item2);

        lb.setAccessLists(networkItems);
        zxtmAdapter.updateAccessList(config, lb);

        final String[][] bannedAddresses = getServiceStubs().getProtectionBinding().getBannedAddresses(new String[]{protectionClassName()});
        Assert.assertEquals(1, bannedAddresses.length);
        Assert.assertEquals(1, bannedAddresses[0].length);
        Assert.assertEquals(item1.getIpAddress(), bannedAddresses[0][0]);

        final String[][] allowedAddresses = getServiceStubs().getProtectionBinding().getAllowedAddresses(new String[]{protectionClassName()});
        Assert.assertEquals(1, allowedAddresses.length);
        Assert.assertEquals(1, allowedAddresses[0].length);
        Assert.assertEquals(item2.getIpAddress(), allowedAddresses[0][0]);
    }

    @Test
    public void testHealthMonitorOperations() throws Exception {
        updateHttpHealthMonitor();
        removeHttpHealthMonitor();
        updateHttpsHealthMonitor();
        removeHttpsHealthMonitor();
    }

    private void updateHttpHealthMonitor() throws Exception {
        HealthMonitor monitor = new HealthMonitor();
        monitor.setType(HealthMonitorType.HTTP);
        monitor.setAttemptsBeforeDeactivation(10);
        monitor.setBodyRegex("");
        monitor.setStatusRegex("");
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

        final String[] bodyRegexArray = getServiceStubs().getMonitorBinding().getBodyRegex(new String[]{monitorName});
        Assert.assertEquals(1, bodyRegexArray.length);
        Assert.assertEquals(monitor.getBodyRegex(), bodyRegexArray[0]);

        final String[] statusRegexArray = getServiceStubs().getMonitorBinding().getStatusRegex(new String[]{monitorName});
        Assert.assertEquals(1, statusRegexArray.length);
        Assert.assertEquals(monitor.getStatusRegex(), statusRegexArray[0]);

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

    private void removeHttpHealthMonitor() throws Exception {
        zxtmAdapter.removeHealthMonitor(config, lb);

        String monitorName = monitorName();
        String[] allMonitorNames = getServiceStubs().getMonitorBinding().getAllMonitorNames();

        for (String someMonitorName : allMonitorNames) {
            if (someMonitorName.equals(monitorName)) Assert.fail("Monitor should not exist.");
        }
    }

    private void updateHttpsHealthMonitor() throws Exception {
        HealthMonitor monitor = new HealthMonitor();
        monitor.setType(HealthMonitorType.HTTPS);
        monitor.setAttemptsBeforeDeactivation(10);
        monitor.setBodyRegex("");
        monitor.setStatusRegex("");
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

        final String[] bodyRegexArray = getServiceStubs().getMonitorBinding().getBodyRegex(new String[]{monitorName});
        Assert.assertEquals(1, bodyRegexArray.length);
        Assert.assertEquals(monitor.getBodyRegex(), bodyRegexArray[0]);

        final String[] statusRegexArray = getServiceStubs().getMonitorBinding().getStatusRegex(new String[]{monitorName});
        Assert.assertEquals(1, statusRegexArray.length);
        Assert.assertEquals(monitor.getStatusRegex(), statusRegexArray[0]);

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
        zxtmAdapter.removeHealthMonitor(config, lb);

        String monitorName = monitorName();
        String[] allMonitorNames = getServiceStubs().getMonitorBinding().getAllMonitorNames();

        for (String someMonitorName : allMonitorNames) {
            if (someMonitorName.equals(monitorName)) Assert.fail("Monitor should not exist.");
        }
    }

    @Test
    public void testBackupOperations() throws Exception {
        createBackup();
        // Not testing restore backup as it may mess with other developers.
        deleteBackup();
    }

    private void createBackup() throws Exception {
        String backupName = "backup";
        zxtmAdapter.createHostBackup(config, backupName);

        final SystemBackupsBackup[] backupDetails = getServiceStubs().getSystemBackupsBinding().getBackupDetails(new String[]{backupName});
        Assert.assertEquals(1, backupDetails.length);
        Assert.assertEquals(backupName, backupDetails[0].getName());
    }

    private void deleteBackup() throws Exception {
        String backupName = "backup";
        zxtmAdapter.deleteHostBackup(config, backupName);

        final SystemBackupsBackup[] allBackups = getServiceStubs().getSystemBackupsBinding().listAllBackups();

        for (SystemBackupsBackup backup : allBackups) {
            if (backup.getName().equals(backupName)) Assert.fail("Backup should not exist!");
        }
    }

    @Test
    public void getStatsSystemLoadBalancerNames() throws Exception {
        List<String> loadBalancerNames = zxtmAdapter.getStatsSystemLoadBalancerNames(config);
        System.out.println(loadBalancerNames.size() + " loadbalancers on host machine.");
        for (String loadBalancerName : loadBalancerNames) {
            System.out.println(loadBalancerName);
        }
    }

    @Test
    public void getLoadBalancerCurrentConnections() throws Exception {
        List<String> loadBalancerNames = zxtmAdapter.getStatsSystemLoadBalancerNames(config);
        Map<String, Integer> concurrentConnections = zxtmAdapter.getLoadBalancerCurrentConnections(config, loadBalancerNames);
        System.out.println("Listing concurrent connections...");
        for (String loadBalancerName : concurrentConnections.keySet()) {
            System.out.println(String.format("LB Name: %s, Concurrent Connections: %d", loadBalancerName, concurrentConnections.get(loadBalancerName)));
        }
    }

    @Test
    public void getTotalCurrentConnectionsForHost() throws Exception {
        int concurrentConnections = zxtmAdapter.getTotalCurrentConnectionsForHost(config);
        System.out.println("Listing total current connections..." + concurrentConnections);
    }

    @Test
    public void getLoadBalancerBytesIn() throws Exception {
        List<String> loadBalancerNames = zxtmAdapter.getStatsSystemLoadBalancerNames(config);
        Map<String, Long> bandwidthBytesIn = zxtmAdapter.getLoadBalancerBytesIn(config, loadBalancerNames);
        System.out.println("Listing bandwidth bytes in...");
        for (String loadBalancerName : bandwidthBytesIn.keySet()) {
            System.out.println(String.format("LB Name: %s, Bandwidth Bytes In: %d", loadBalancerName, bandwidthBytesIn.get(loadBalancerName)));
        }
    }

    @Test
    public void getLoadBalancerBytesOut() throws Exception {
        List<String> loadBalancerNames = zxtmAdapter.getStatsSystemLoadBalancerNames(config);
        Map<String, Long> bandwidthBytesOut = zxtmAdapter.getLoadBalancerBytesIn(config, loadBalancerNames);
        System.out.println("Listing bandwidth bytes out...");
        for (String loadBalancerName : bandwidthBytesOut.keySet()) {
            System.out.println(String.format("LB Name: %s, Bandwidth Bytes Out: %d", loadBalancerName, bandwidthBytesOut.get(loadBalancerName)));
        }
    }

    @Test
    public void getHostBytesIn() throws RemoteException {
        long bytesIn = zxtmAdapter.getHostBytesIn(config);
        System.out.println(String.format("Host Name: %s, Bandwidth Bytes In: %d", config.getTrafficManagerName(), bytesIn));
    }

    @Test
    public void getHostBytesOut() throws RemoteException {
        long bytesOut = zxtmAdapter.getHostBytesOut(config);
        System.out.println(String.format("Host Name: %s, Bandwidth Bytes Out: %d", config.getTrafficManagerName(), bytesOut));
    }
}