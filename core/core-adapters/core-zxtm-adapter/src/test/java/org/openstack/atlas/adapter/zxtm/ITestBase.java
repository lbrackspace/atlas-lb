package org.openstack.atlas.adapter.zxtm;

import com.zxtm.service.client.*;
import org.apache.axis.AxisFault;
import org.junit.Assert;
import org.openstack.atlas.adapter.LoadBalancerAdapter;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exception.BadRequestException;
import org.openstack.atlas.adapter.zxtm.helper.IpHelper;
import org.openstack.atlas.adapter.zxtm.helper.ZxtmNameBuilder;
import org.openstack.atlas.adapter.zxtm.service.ZxtmServiceStubs;
import org.openstack.atlas.service.domain.entity.*;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ITestBase {
    public static final Integer SLEEP_TIME_BETWEEN_TESTS = 500;

    // TODO: get this from external config...
    public static final String ZXTM_USERNAME = "username";
    public static final String ZXTM_PASSWORD = "password";
    public static final String ZXTM_ENDPOINT_URI = "https://1.2.3.4:1234/soap";
    public static final String TARGET_HOST = "ztm-n01.test.com";
    public static final String FAILOVER_HOST_1 = "ztm-n03.test.com";
    public static final String FAILOVER_HOST_2 = "ztm-n04.test.com";
    public static final String DEFAULT_LOG_FILE_LOCATION = "/opt/zeus/zxtm/log/access_log";
    public static final Integer TEST_ACCOUNT_ID = 999999;
    public static final Integer TEST_LOADBALANCER_ID = 999999;
    public static final Integer TEST_VIP_ID = 999999;
    public static final Integer TEST_IPV6_VIP_ID = 999996;
    public static final Integer ADDITIONAL_VIP_ID = 88888;
    public static final Integer ADDITIONAL_IPV6_VIP_ID = 88886;

    protected static LoadBalancerAdapter zxtmAdapter;
    protected static LoadBalancerEndpointConfiguration config;
    protected static LoadBalancer lb;
    protected static VirtualIp vip1;
    protected static Node node1;
    protected static Node node2;
    protected static Cluster cluster;

    static {
        zxtmAdapter = new ZxtmAdapterImpl();
        try {
            setupEndpointConfiguration();
        } catch (MalformedURLException e) {
            Assert.fail(e.getMessage());
        }
        setUpClusterForIPv6Operations();
    }

    private static void setupEndpointConfiguration() throws MalformedURLException {
        List<String> targetFailoverHosts = new ArrayList<String>();
        targetFailoverHosts.add(FAILOVER_HOST_1);
        targetFailoverHosts.add(FAILOVER_HOST_2);
        Host soapEndpointHost = new Host();
        soapEndpointHost.setEndpoint(ZXTM_ENDPOINT_URI);
        Host trafficManagerHost = new Host();
        trafficManagerHost.setName(TARGET_HOST);
        config = new LoadBalancerEndpointConfiguration(soapEndpointHost, ZXTM_USERNAME, ZXTM_PASSWORD, trafficManagerHost, targetFailoverHosts);
        config.setLogFileLocation(DEFAULT_LOG_FILE_LOCATION);
    }

    private static void setUpClusterForIPv6Operations() {
        cluster = new Cluster();
        cluster.setClusterIpv6Cidr("fd24:f480:ce44:91bc::/64");
    }

    protected static void setupIvars() {
        Set<LoadBalancerJoinVip> vipList = new HashSet<LoadBalancerJoinVip>();
        vip1 = new VirtualIp();
        vip1.setId(TEST_VIP_ID);
        vip1.setAddress("10.69.0.60");
        LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip();
        loadBalancerJoinVip.setVirtualIp(vip1);
        vipList.add(loadBalancerJoinVip);

        Set<Node> nodeList = new HashSet<Node>();
        node1 = new Node();
        node2 = new Node();
        node1.setAddress("127.0.0.1");
        node2.setAddress("127.0.0.2");
        node1.setPort(80);
        node2.setPort(80);
        node1.setEnabled(true);
        node2.setEnabled(false);
        nodeList.add(node1);
        nodeList.add(node2);

        LoadBalancer lb = new LoadBalancer();
        lb.setId(TEST_LOADBALANCER_ID);
        lb.setAccountId(TEST_ACCOUNT_ID);
        lb.setName("integration_test_lb");
        lb.setNodes(nodeList);
        lb.setLoadBalancerJoinVipSet(vipList);

        ITestBase.lb = lb;
    }

    protected static ZxtmServiceStubs getServiceStubs() throws AxisFault {
        return ZxtmServiceStubs.getServiceStubs(config.getEndpointUrl(), config.getUsername(), config.getPassword());
    }

    protected static String loadBalancerName() throws BadRequestException {
        return ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lb);
    }

    protected static String poolName() throws BadRequestException {
        return ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lb);
    }

    protected static String protectionClassName() throws BadRequestException {
        return ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lb);
    }

    protected static String trafficIpGroupName(VirtualIp vip) throws BadRequestException {
        return ZxtmNameBuilder.generateTrafficIpGroupName(lb, vip);
    }

    protected static String trafficIpGroupName(VirtualIpv6 ipv6Vip) throws BadRequestException {
        return ZxtmNameBuilder.generateTrafficIpGroupName(lb, ipv6Vip);
    }

    protected static String rateLimitName() throws BadRequestException {
        return ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lb);
    }

    protected static String monitorName() throws BadRequestException {
        return ZxtmNameBuilder.generateNameWithAccountIdAndLoadBalancerId(lb);
    }

    protected static void setupSimpleLoadBalancer() {
        shouldBeValidApiVersion();
        createSimpleLoadBalancer();
    }

    protected static void removeSimpleLoadBalancer() {
        removeLoadBalancer();
    }

    private static void shouldBeValidApiVersion() {
        String ZEUS_API_VERSION = "7.3r1";
        try {
            Assert.assertEquals(ZEUS_API_VERSION, getServiceStubs().getSystemMachineInfoBinding().getProductVersion());
        } catch (RemoteException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    private static void createSimpleLoadBalancer() {
        try {
            zxtmAdapter.createLoadBalancer(config, lb.getAccountId(), lb);

            final VirtualServerBasicInfo[] virtualServerBasicInfos = getServiceStubs().getVirtualServerBinding().getBasicInfo(new String[]{loadBalancerName()});
            Assert.assertEquals(1, virtualServerBasicInfos.length);
            Assert.assertEquals(VirtualServerProtocol.http, virtualServerBasicInfos[0].getProtocol());
            Assert.assertEquals(lb.getPort().intValue(), virtualServerBasicInfos[0].getPort());
            Assert.assertEquals(poolName(), virtualServerBasicInfos[0].getDefault_pool());

            String trafficIpGroupName = trafficIpGroupName(lb.getLoadBalancerJoinVipSet().iterator().next().getVirtualIp());

            final String[][] trafficManagers = getServiceStubs().getTrafficIpGroupBinding().getTrafficManager(new String[]{trafficIpGroupName});
            Assert.assertEquals(1, trafficManagers.length);
            Assert.assertEquals(3, trafficManagers[0].length);

            final String[][] vips = getServiceStubs().getTrafficIpGroupBinding().getIPAddresses(new String[]{trafficIpGroupName});
            Assert.assertEquals(1, vips.length);
            Assert.assertEquals(1, vips[0].length);
            Assert.assertEquals(vip1.getAddress(), vips[0][0]);

            final String[][] enabledNodes = getServiceStubs().getPoolBinding().getNodes(new String[]{poolName()});
            Assert.assertEquals(1, enabledNodes.length);
            Assert.assertEquals(1, enabledNodes[0].length);
            Assert.assertEquals(IpHelper.createZeusIpString(node1.getAddress(), node1.getPort()), enabledNodes[0][0]);

            final String[][] disabledNodes = getServiceStubs().getPoolBinding().getDisabledNodes(new String[]{poolName()});
            Assert.assertEquals(1, disabledNodes.length);
            Assert.assertEquals(1, disabledNodes[0].length);
            Assert.assertEquals(IpHelper.createZeusIpString(node2.getAddress(), node2.getPort()), disabledNodes[0][0]);

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

            final VirtualServerRule[][] virtualServerRules = getServiceStubs().getVirtualServerBinding().getRules(new String[]{loadBalancerName()});
            Assert.assertEquals(1, virtualServerRules.length);
            Assert.assertEquals(1, virtualServerRules[0].length);
            Assert.assertEquals(ZxtmAdapterImpl.ruleXForwardedFor, virtualServerRules[0][0]);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }


    protected static void removeLoadBalancer() {
        try {
            zxtmAdapter.deleteLoadBalancer(config, lb.getAccountId(), lb.getId());
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
            String trafficIpGroupName = trafficIpGroupName(lb.getLoadBalancerJoinVipSet().iterator().next().getVirtualIp());
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
