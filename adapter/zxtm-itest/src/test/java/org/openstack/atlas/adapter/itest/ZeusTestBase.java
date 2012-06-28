package org.openstack.atlas.adapter.itest;

import Util.ConfigurationKeys;
import Util.EsbConfiguration;
import com.zxtm.service.client.*;
import org.apache.axis.AxisFault;
import org.junit.Assert;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.helpers.IpHelper;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerAdapter;
import org.openstack.atlas.adapter.zxtm.ZxtmAdapterImpl;
import org.openstack.atlas.adapter.zxtm.ZxtmServiceStubs;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.util.ca.primitives.RsaConst;
import org.xml.sax.SAXException;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm.ROUND_ROBIN;
import static org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.HTTP;
import static org.openstack.atlas.service.domain.entities.NodeCondition.DISABLED;
import static org.openstack.atlas.service.domain.entities.NodeCondition.ENABLED;

public class ZeusTestBase {
    public static final Integer SLEEP_TIME_BETWEEN_TESTS = 500;
    private static Configuration configuration = new EsbConfiguration();

    public static String ZXTM_USERNAME;
    public static String ZXTM_PASSWORD;
    public static String ZXTM_ENDPOINT_URI;
    public static String TARGET_HOST;
    public static String FAILOVER_HOST_1;
    public static String FAILOVER_HOST_2;
    public static String DEFAULT_LOG_FILE_LOCATION;

    public static final Integer TEST_ACCOUNT_ID = 999998;
    public static final Integer TEST_LOADBALANCER_ID = 999998;
    public static final Integer TEST_VIP_ID = 999998;
    public static final Integer TEST_IPV6_VIP_ID = 999995;
    public static final Integer ADDITIONAL_VIP_ID = 88887;
    public static final Integer ADDITIONAL_IPV6_VIP_ID = 88885;

    protected static ReverseProxyLoadBalancerAdapter zxtmAdapter;
    protected static LoadBalancerEndpointConfiguration config;
    protected static LoadBalancer lb;
    protected static VirtualIp vip1;
    protected static Node node1;
    protected static Node node2;
    protected static Cluster cluster;

    static {
        zxtmAdapter = new ZxtmAdapterImpl();
        try {
            retrieveConfigValues();
            setupEndpointConfiguration();
            RsaConst.init();
        } catch (MalformedURLException e) {
            Assert.fail(e.getMessage());
        }
        setUpClusterForIPv6Operations();
    }

    private static void retrieveConfigValues() {
        ZXTM_USERNAME = configuration.getString(ConfigurationKeys.zxtm_username);
        ZXTM_PASSWORD = configuration.getString(ConfigurationKeys.zxtm_password);
        ZXTM_ENDPOINT_URI = configuration.getString(ConfigurationKeys.zxtm_endpoint_uri);
        TARGET_HOST = configuration.getString(ConfigurationKeys.target_host);
        FAILOVER_HOST_1 = configuration.getString(ConfigurationKeys.failover_host_1);
        FAILOVER_HOST_2 = configuration.getString(ConfigurationKeys.failover_host_2);
        DEFAULT_LOG_FILE_LOCATION = configuration.getString(ConfigurationKeys.default_log_file_location);
    }

    private static void setupEndpointConfiguration() throws MalformedURLException {
        List<String> targetFailoverHosts = new ArrayList<String>();
        targetFailoverHosts.add(FAILOVER_HOST_1);
//        targetFailoverHosts.add(FAILOVER_HOST_2);
        Host soapEndpointHost = new Host();
        soapEndpointHost.setEndpoint(ZXTM_ENDPOINT_URI);
        Host trafficManagerHost = new Host();
        trafficManagerHost.setTrafficManagerName(TARGET_HOST);
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
        vip1.setIpAddress("10.69.0.59");
        LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip();
        loadBalancerJoinVip.setVirtualIp(vip1);
        vipList.add(loadBalancerJoinVip);

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
        lb.setName("integration_test_lb");
        lb.setProtocol(HTTP);
        lb.setNodes(nodeList);
        lb.setLoadBalancerJoinVipSet(vipList);

        ZeusTestBase.lb = lb;
    }

    protected static ZxtmServiceStubs getServiceStubs() throws AxisFault {
        return ZxtmServiceStubs.getServiceStubs(config.getEndpointUrl(), config.getUsername(), config.getPassword());
    }

    protected static String loadBalancerName() throws InsufficientRequestException {
        return ZxtmNameBuilder.genVSName(lb);
    }

    protected static String secureLoadBalancerName() throws InsufficientRequestException {
        return ZxtmNameBuilder.genSslVSName(lb.getId(), lb.getAccountId());
    }

    protected static String poolName() throws InsufficientRequestException {
        return ZxtmNameBuilder.genVSName(lb);
    }

    protected static String protectionClassName() throws InsufficientRequestException {
        return ZxtmNameBuilder.genVSName(lb);
    }

    protected static String secureProtectionClassName() throws InsufficientRequestException {
        return ZxtmNameBuilder.genSslVSName(lb);
    }

    protected static String trafficIpGroupName(VirtualIp vip) throws InsufficientRequestException {
        return ZxtmNameBuilder.generateTrafficIpGroupName(lb, vip);
    }

    protected static String trafficIpGroupName(VirtualIpv6 ipv6Vip) throws InsufficientRequestException {
        return ZxtmNameBuilder.generateTrafficIpGroupName(lb, ipv6Vip);
    }

    protected static String rateLimitName() throws InsufficientRequestException {
        return ZxtmNameBuilder.genVSName(lb);
    }

    protected static String errorFileName() throws InsufficientRequestException {
        return ZxtmNameBuilder.generateErrorPageNameWithAccountIdAndLoadBalancerId(lb.getId(), lb.getAccountId());
    }

    protected static String monitorName() throws InsufficientRequestException {
        return ZxtmNameBuilder.genVSName(lb);
    }

    protected static void setupSimpleLoadBalancer() {
        shouldBeValidApiVersion();
        createSimpleLoadBalancer();
    }

    protected static void removeSimpleLoadBalancer() {
        removeLoadBalancer();
    }

    protected static void shouldBeValidApiVersion() {
        String ZEUS_API_VERSION = "9.0";
        try {
            Assert.assertEquals(ZEUS_API_VERSION, getServiceStubs().getSystemMachineInfoBinding().getProductVersion());
        } catch (RemoteException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    private static void createSimpleLoadBalancer() {
        try {
            zxtmAdapter.createLoadBalancer(config, lb);

            final VirtualServerBasicInfo[] virtualServerBasicInfos = getServiceStubs().getVirtualServerBinding().getBasicInfo(new String[]{loadBalancerName()});
            Assert.assertEquals(1, virtualServerBasicInfos.length);
            Assert.assertEquals(VirtualServerProtocol.http, virtualServerBasicInfos[0].getProtocol());
            Assert.assertEquals(lb.getPort().intValue(), virtualServerBasicInfos[0].getPort());
            Assert.assertEquals(poolName(), virtualServerBasicInfos[0].getDefault_pool());

            String trafficIpGroupName = trafficIpGroupName(lb.getLoadBalancerJoinVipSet().iterator().next().getVirtualIp());

            final String[][] trafficManagers = getServiceStubs().getTrafficIpGroupBinding().getTrafficManager(new String[]{trafficIpGroupName});
            Assert.assertEquals(1, trafficManagers.length);
            Assert.assertEquals(2, trafficManagers[0].length);

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

            final VirtualServerRule[][] virtualServerRules = getServiceStubs().getVirtualServerBinding().getRules(new String[]{loadBalancerName()});
            Assert.assertEquals(1, virtualServerRules.length);
            Assert.assertEquals(2, virtualServerRules[0].length);
            Assert.assertEquals(ZxtmAdapterImpl.ruleXForwardedFor, virtualServerRules[0][0]);

            final String[] errorFile = getServiceStubs().getVirtualServerBinding().getErrorFile(new String[]{loadBalancerName()});
            Assert.assertEquals("Default", errorFile[0]);

        } catch (Exception e) {
            if (e instanceof ObjectAlreadyExists) {
                removeLoadBalancer();
            }
            e.printStackTrace();
            Assert.fail(e.getMessage());

        }
    }


    protected static void removeLoadBalancer() {
        try {
            zxtmAdapter.deleteLoadBalancer(config, lb);
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
            }
            if (e instanceof SAXException) {
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
