package org.openstack.atlas.adapter.itest;

import com.zxtm.service.client.PoolLoadBalancingAlgorithm;
import com.zxtm.service.client.PoolWeightingsDefinition;
import com.zxtm.service.client.VirtualServerBasicInfo;
import com.zxtm.service.client.VirtualServerProtocol;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.adapter.helpers.IpHelper;

import static org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.HTTP;
import static org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.HTTPS;

@RunWith(Enclosed.class)
public class UpdateProtocolIntegrationTest extends ZeusTestBase {
    public static class testingProtocolChangeFromHTTP {
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
        public void changeProtocolWithConnectionLoggingEnabledFromHTTP() {
            try {
                VirtualServerProtocol[] protocols = getServiceStubs().getVirtualServerBinding().getProtocol(new String[]{loadBalancerName()});
                Assert.assertEquals(1, protocols.length);
                Assert.assertEquals(VirtualServerProtocol.http, protocols[0]);

                zxtmAdapter.updateConnectionLogging(config, lb.getId(), lb.getAccountId(), true, lb.getProtocol());
                boolean[] logEnabled = getServiceStubs().getVirtualServerBinding().getLogEnabled(new String[]{loadBalancerName()});
                Assert.assertEquals(1, logEnabled.length);
                Assert.assertEquals(true, logEnabled[0]);

                zxtmAdapter.updateProtocol(config, lb.getId(), lb.getAccountId(), HTTPS);
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }
    }
    
    public static class testingProtocolChangeToHTTP {
        @BeforeClass
        public static void setupClass() throws InterruptedException {
            Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
            setupIvarsWithHTTPS();
            shouldBeValidApiVersion();
            createLoadBalancer();
        }

        @AfterClass
        public static void tearDownClass() {
            removeSimpleLoadBalancer();
        }

        @Test
        public void changeProtocolWithConnectionLoggingEnabledToHTTP() {
            try {
                VirtualServerProtocol[] protocols = getServiceStubs().getVirtualServerBinding().getProtocol(new String[]{loadBalancerName()});
                Assert.assertEquals(1, protocols.length);
                Assert.assertEquals(VirtualServerProtocol.https, protocols[0]);

                zxtmAdapter.updateConnectionLogging(config, lb.getId(), lb.getAccountId(), true, lb.getProtocol());
                boolean[] logEnabled = getServiceStubs().getVirtualServerBinding().getLogEnabled(new String[]{loadBalancerName()});
                Assert.assertEquals(1, logEnabled.length);
                Assert.assertEquals(true, logEnabled[0]);
                
                zxtmAdapter.updateProtocol(config, lb.getId(), lb.getAccountId(), HTTP);
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }
    }

    protected static void setupIvarsWithHTTPS() {
        setupIvars();
        ZeusTestBase.lb.setProtocol(HTTPS);
    }

    protected static void createLoadBalancer() {
        try {
            zxtmAdapter.createLoadBalancer(config, lb);

            final VirtualServerBasicInfo[] virtualServerBasicInfos = getServiceStubs().getVirtualServerBinding().getBasicInfo(new String[]{loadBalancerName()});
            Assert.assertEquals(1, virtualServerBasicInfos.length);
            Assert.assertEquals(VirtualServerProtocol.https, virtualServerBasicInfos[0].getProtocol());
            Assert.assertEquals(lb.getPort().intValue(), virtualServerBasicInfos[0].getPort());
            Assert.assertEquals(poolName(), virtualServerBasicInfos[0].getDefault_pool());

            String trafficIpGroupName = trafficIpGroupName(lb.getLoadBalancerJoinVipSet().iterator().next().getVirtualIp());

            final String[][] trafficManagers = getServiceStubs().getTrafficIpGroupBinding().getTrafficManager(new String[]{trafficIpGroupName});
            Assert.assertEquals(1, trafficManagers.length);
            Assert.assertEquals(3, trafficManagers[0].length);

            final String[][] vips = getServiceStubs().getTrafficIpGroupBinding().getIPAddresses(new String[]{trafficIpGroupName});
            Assert.assertEquals(1, vips.length);
            Assert.assertEquals(1, vips[0].length);
            Assert.assertEquals(vip1.getIpAddress(), vips[0][0]);

            final String[][] enabledNodes = getServiceStubs().getPoolBinding().getNodes(new String[]{poolName()});
            Assert.assertEquals(1, enabledNodes.length);
            Assert.assertEquals(1, enabledNodes[0].length);
            Assert.assertEquals(IpHelper.createZeusIpString(node1.getIpAddress(), node1.getPort()), enabledNodes[0][0]);

            final String[][] disabledNodes = getServiceStubs().getPoolBinding().getDisabledNodes(new String[]{poolName()});
            Assert.assertEquals(1, disabledNodes.length);
            Assert.assertEquals(1, disabledNodes[0].length);
            Assert.assertEquals(IpHelper.createZeusIpString(node2.getIpAddress(), node2.getPort()), disabledNodes[0][0]);

            final String[][] drainingNodes = getServiceStubs().getPoolBinding().getDrainingNodes(new String[]{poolName()});
            Assert.assertEquals(1, drainingNodes.length);
            Assert.assertEquals(0, drainingNodes[0].length);

            final PoolWeightingsDefinition[][] enabledNodeWeights = getServiceStubs().getPoolBinding().getNodesWeightings(new String[]{poolName()}, enabledNodes);
            Assert.assertEquals(1, enabledNodeWeights.length);
            Assert.assertEquals(1, enabledNodeWeights[0].length);
            Assert.assertEquals(1, enabledNodeWeights[0][0].getWeighting());

            final PoolWeightingsDefinition[][] disabledNodeWeights = getServiceStubs().getPoolBinding().getNodesWeightings(new String[]{poolName()}, disabledNodes);
            Assert.assertEquals(1, disabledNodeWeights.length);
            Assert.assertEquals(1, disabledNodeWeights[0].length);
            Assert.assertEquals(1, disabledNodeWeights[0][0].getWeighting());

            final PoolWeightingsDefinition[][] drainingNodeWeights = getServiceStubs().getPoolBinding().getNodesWeightings(new String[]{poolName()}, drainingNodes);
            Assert.assertEquals(1, drainingNodeWeights.length);
            Assert.assertEquals(0, drainingNodeWeights[0].length);

            final PoolLoadBalancingAlgorithm[] algorithms = getServiceStubs().getPoolBinding().getLoadBalancingAlgorithm(new String[]{poolName()});
            Assert.assertEquals(1, algorithms.length);
            Assert.assertEquals(PoolLoadBalancingAlgorithm.roundrobin.toString(), algorithms[0].getValue());

            final String[] errorFile = getServiceStubs().getVirtualServerBinding().getErrorFile(new String[]{loadBalancerName()});
            Assert.assertEquals("Default", errorFile[0]);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}