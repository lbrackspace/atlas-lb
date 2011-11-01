package org.openstack.atlas.adapter.zxtm;

import com.zxtm.service.client.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.adapter.zxtm.helper.IpHelper;

public class SharedIpITest extends ITestBase {

    @BeforeClass
    public static void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupLb1();
        setupLb2();
        setupSimpleLoadBalancer();
        createLoadBalancerWithSharedIp();
    }

    @AfterClass
    public static void tearDownClass() {
        removeLoadBalancerWithSharedIp();
        removeSimpleLoadBalancer();
    }

    @Test
    public void trafficIpGroupShouldBeUsedByBothLoadBalancers() throws Exception {
        final String[][] trafficIPGroups1 = getServiceStubs().getVirtualServerBinding().getListenTrafficIPGroups(new String[]{loadBalancerName(lb_1)});
        final String[][] trafficIPGroups2 = getServiceStubs().getVirtualServerBinding().getListenTrafficIPGroups(new String[]{loadBalancerName(lb_2)});
        Assert.assertTrue(trafficIPGroups1[0][0].equals(trafficIPGroups2[0][0]));
    }

    private static void createLoadBalancerWithSharedIp() {
        try {
            zxtmAdapter.createLoadBalancer(config, lb_2);

            final VirtualServerBasicInfo[] virtualServerBasicInfos = getServiceStubs().getVirtualServerBinding().getBasicInfo(new String[]{loadBalancerName(lb_2)});
            Assert.assertEquals(1, virtualServerBasicInfos.length);
            Assert.assertEquals(VirtualServerProtocol.http, virtualServerBasicInfos[0].getProtocol());
            Assert.assertEquals(lb_2.getPort().intValue(), virtualServerBasicInfos[0].getPort());
            Assert.assertEquals(poolName(lb_2), virtualServerBasicInfos[0].getDefault_pool());

            String trafficIpGroupName = trafficIpGroupName(lb_2.getLoadBalancerJoinVipSet().iterator().next().getVirtualIp());

            final String[][] trafficManagers = getServiceStubs().getTrafficIpGroupBinding().getTrafficManager(new String[]{trafficIpGroupName});
            Assert.assertEquals(1, trafficManagers.length);
            Assert.assertEquals(3, trafficManagers[0].length);

            final String[][] vips = getServiceStubs().getTrafficIpGroupBinding().getIPAddresses(new String[]{trafficIpGroupName});
            Assert.assertEquals(1, vips.length);
            Assert.assertEquals(1, vips[0].length);
            Assert.assertEquals(vip_1_1.getAddress(), vips[0][0]);

            final String[][] enabledNodes = getServiceStubs().getPoolBinding().getNodes(new String[]{poolName(lb_2)});
            Assert.assertEquals(1, enabledNodes.length);
            Assert.assertEquals(1, enabledNodes[0].length);
            Assert.assertEquals(IpHelper.createZeusIpString(node_1_1.getAddress(), node_1_1.getPort()), enabledNodes[0][0]);

            final String[][] disabledNodes = getServiceStubs().getPoolBinding().getDisabledNodes(new String[]{poolName(lb_2)});
            Assert.assertEquals(1, disabledNodes.length);
            Assert.assertEquals(1, disabledNodes[0].length);
            Assert.assertEquals(IpHelper.createZeusIpString(node_1_2.getAddress(), node_1_2.getPort()), disabledNodes[0][0]);

            final String[][] drainingNodes = getServiceStubs().getPoolBinding().getDrainingNodes(new String[]{poolName(lb_2)});
            Assert.assertEquals(1, drainingNodes.length);
            Assert.assertEquals(0, drainingNodes[0].length);

            final PoolWeightingsDefinition[][] enabledNodeWeights = getServiceStubs().getPoolBinding().getNodesWeightings(new String[]{poolName(lb_2)}, enabledNodes);
            Assert.assertEquals(1, enabledNodeWeights.length);
            Assert.assertEquals(1, enabledNodeWeights[0].length);
            Assert.assertEquals(1, enabledNodeWeights[0][0].getWeighting());

            final PoolWeightingsDefinition[][] disabledNodeWeights = getServiceStubs().getPoolBinding().getNodesWeightings(new String[]{poolName(lb_2)}, disabledNodes);
            Assert.assertEquals(1, disabledNodeWeights.length);
            Assert.assertEquals(1, disabledNodeWeights[0].length);
            Assert.assertEquals(1, disabledNodeWeights[0][0].getWeighting());

            final PoolWeightingsDefinition[][] drainingNodeWeights = getServiceStubs().getPoolBinding().getNodesWeightings(new String[]{poolName(lb_2)}, drainingNodes);
            Assert.assertEquals(1, drainingNodeWeights.length);
            Assert.assertEquals(0, drainingNodeWeights[0].length);

            final PoolLoadBalancingAlgorithm[] algorithms = getServiceStubs().getPoolBinding().getLoadBalancingAlgorithm(new String[]{poolName(lb_2)});
            Assert.assertEquals(1, algorithms.length);
            Assert.assertEquals(PoolLoadBalancingAlgorithm.roundrobin.toString(), algorithms[0].getValue());

            final VirtualServerRule[][] virtualServerRules = getServiceStubs().getVirtualServerBinding().getRules(new String[]{loadBalancerName(lb_2)});
            Assert.assertEquals(1, virtualServerRules.length);
            Assert.assertEquals(1, virtualServerRules[0].length);
            Assert.assertEquals(ZxtmAdapterImpl.ruleXForwardedFor, virtualServerRules[0][0]);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    private static void removeLoadBalancerWithSharedIp() {
        try {
            zxtmAdapter.deleteLoadBalancer(config, lb_2);
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
            } else {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }

        try {
            getServiceStubs().getVirtualServerBinding().getBasicInfo(new String[]{loadBalancerName(lb_2)});
            Assert.fail("Virtual Server should have been deleted!");
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
            } else {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }

        try {
            getServiceStubs().getPoolBinding().getNodes(new String[]{poolName(lb_2)});
            Assert.fail("Node Pool should have been deleted!");
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
            } else {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }

        try {
            String trafficIpGroupName = trafficIpGroupName(lb_2.getLoadBalancerJoinVipSet().iterator().next().getVirtualIp());
            final String[][] ipAddresses = getServiceStubs().getTrafficIpGroupBinding().getIPAddresses(new String[]{trafficIpGroupName});
            Assert.assertEquals(1, ipAddresses.length);
            Assert.assertEquals(1, ipAddresses[0].length);
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
            } else {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }
    }
}
