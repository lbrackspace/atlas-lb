package org.openstack.atlas.adapter.itest;

import com.zxtm.service.client.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.adapter.helpers.IpHelper;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip6;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.VirtualIpv6;
import org.openstack.atlas.util.ip.IPv6;

import java.util.HashSet;
import java.util.Set;

import static org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm.ROUND_ROBIN;
import static org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.HTTP;
import static org.openstack.atlas.service.domain.entities.NodeCondition.DISABLED;
import static org.openstack.atlas.service.domain.entities.NodeCondition.ENABLED;

/*
 * IMPORTANT! PLEASE READ!
 * Order matters when running this test so please be careful.
 */
public class Ipv6IntegrationTest extends ZeusTestBase {
    protected static VirtualIpv6 vip1;

    @BeforeClass
    public static void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
    }

    protected static void setupIvars() {
        Set<LoadBalancerJoinVip6> ipv6VipList = new HashSet<LoadBalancerJoinVip6>();
        vip1 = new VirtualIpv6();
        vip1.setId(TEST_IPV6_VIP_ID);
        vip1.setAccountId(TEST_ACCOUNT_ID);
        vip1.setCluster(cluster);
        vip1.setVipOctets(1);
        LoadBalancerJoinVip6 loadBalancerJoinVip = new LoadBalancerJoinVip6();
        loadBalancerJoinVip.setVirtualIp(vip1);
        ipv6VipList.add(loadBalancerJoinVip);

        Set<Node> nodeList = new HashSet<Node>();
        node1 = new Node();
        node2 = new Node();
        node1.setIpAddress("127.0.0.1");
        node2.setIpAddress("127.0.0.2");
        node1.setPort(80);
        node2.setPort(80);
        node1.setCondition(ENABLED);
        node2.setCondition(DISABLED);
        nodeList.add(node1);
        nodeList.add(node2);

        LoadBalancer lb = new LoadBalancer();
        lb.setId(TEST_LOADBALANCER_ID);
        lb.setAccountId(TEST_ACCOUNT_ID);
        lb.setPort(80);
        lb.setAlgorithm(ROUND_ROBIN);
        lb.setName("ipv6_integration_test_lb");
        lb.setProtocol(HTTP);
        lb.setNodes(nodeList);
        lb.setLoadBalancerJoinVip6Set(ipv6VipList);

        Ipv6IntegrationTest.lb = lb;
    }

    @Test
    public void createAndRemoveIpv6LoadBalancer() {
        removeIpv6LoadBalancer();
        createSimpleIpv6LoadBalancer();
        removeIpv6LoadBalancer();
    }

    protected static void createSimpleIpv6LoadBalancer() {
        try {
            zxtmAdapter.createLoadBalancer(config, lb);

            final VirtualServerBasicInfo[] virtualServerBasicInfos = getServiceStubs().getVirtualServerBinding().getBasicInfo(new String[]{loadBalancerName()});
            Assert.assertEquals(1, virtualServerBasicInfos.length);
            Assert.assertEquals(VirtualServerProtocol.http, virtualServerBasicInfos[0].getProtocol());
            Assert.assertEquals(lb.getPort().intValue(), virtualServerBasicInfos[0].getPort());
            Assert.assertEquals(poolName(), virtualServerBasicInfos[0].getDefault_pool());

            String trafficIpGroupName = trafficIpGroupName(lb.getLoadBalancerJoinVip6Set().iterator().next().getVirtualIp());

            final String[][] trafficManagers = getServiceStubs().getTrafficIpGroupBinding().getTrafficManager(new String[]{trafficIpGroupName});
            Assert.assertEquals(1, trafficManagers.length);
            Assert.assertEquals(2, trafficManagers[0].length);

            final String[][] vips = getServiceStubs().getTrafficIpGroupBinding().getIPAddresses(new String[]{trafficIpGroupName});
            Assert.assertEquals(1, vips.length);
            Assert.assertEquals(1, vips[0].length);
            Assert.assertEquals(new IPv6(vip1.getDerivedIpString()).expand(), new IPv6(vips[0][0]).expand());

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

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    protected static void removeIpv6LoadBalancer() {
        try {
            zxtmAdapter.deleteLoadBalancer(config, lb);
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
            } else {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }

        try {
            getServiceStubs().getVirtualServerBinding().getBasicInfo(new String[]{loadBalancerName()});
            Assert.fail("Virtual Server should have been deleted!");
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
            } else {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }

        try {
            getServiceStubs().getPoolBinding().getNodes(new String[]{poolName()});
            Assert.fail("Node Pool should have been deleted!");
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
            } else {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }

        try {
            String trafficIpGroupName = trafficIpGroupName(lb.getLoadBalancerJoinVip6Set().iterator().next().getVirtualIp());
            getServiceStubs().getTrafficIpGroupBinding().getIPAddresses(new String[]{trafficIpGroupName});
            Assert.fail("Traffic Ip Group should have been deleted!");
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
            } else {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }
    }
}
