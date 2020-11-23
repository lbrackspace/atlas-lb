package org.openstack.atlas.adapter.itest;

import Util.ConfigurationKeys;
import Util.VTMConfiguration;
import org.junit.Assert;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.helpers.VTMNameBuilder;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerVTMAdapter;
import org.openstack.atlas.adapter.vtm.VTMResourceTranslator;
import org.openstack.atlas.adapter.vtm.VTMadapterImpl;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.util.ca.primitives.RsaConst;
import org.openstack.atlas.util.crypto.CryptoUtil;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.vtm.client.VTMRestClient;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.vtm.client.list.Child;
import org.rackspace.vtm.client.pool.Pool;
import org.rackspace.vtm.client.pool.PoolLoadbalancing;
import org.rackspace.vtm.client.traffic.ip.TrafficIp;
import org.rackspace.vtm.client.virtualserver.VirtualServer;

import java.net.MalformedURLException;
import java.util.*;

import static org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm.ROUND_ROBIN;
import static org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.HTTP;
import static org.openstack.atlas.service.domain.entities.NodeCondition.DISABLED;
import static org.openstack.atlas.service.domain.entities.NodeCondition.ENABLED;

public class VTMTestBase extends VTMTestConstants {
    public static final Integer SLEEP_TIME_BETWEEN_TESTS = 500;
    private static Configuration configuration = new VTMConfiguration();

    public static String VTM_USERNAME;
    public static String VTM_PASSWORD;
    public static String VTM_ENDPOINT_URI;
    public static String TARGET_HOST;
    public static String FAILOVER_HOST_1;
    public static String DEFAULT_LOG_FILE_LOCATION;

    protected static ReverseProxyLoadBalancerVTMAdapter vtmAdapter;
    protected static VTMRestClient vtmClient;
    protected static LoadBalancerEndpointConfiguration config;
    protected static LoadBalancer lb;
    protected static VirtualIp vip1;
    protected static Node node1;
    protected static Node node2;
    protected static Cluster cluster;

    static {
        vtmAdapter = new VTMadapterImpl();
        try {
            retrieveConfigValues();
            setupEndpointConfiguration();
            RsaConst.init();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        setUpClusterForIPv6Operations();
        vtmClient = new VTMRestClient();
    }

    private static void retrieveConfigValues() {
        VTM_USERNAME = configuration.getString(ConfigurationKeys.stingray_admin_user);
        VTM_PASSWORD = configuration.getString(ConfigurationKeys.stingray_admin_key);
        VTM_ENDPOINT_URI = configuration.getString(ConfigurationKeys.stingray_rest_endpoint)
                + configuration.getString(ConfigurationKeys.stingray_base_uri);
        TARGET_HOST = configuration.getString(ConfigurationKeys.target_host);
        FAILOVER_HOST_1 = configuration.getString(ConfigurationKeys.failover_host_1);
        DEFAULT_LOG_FILE_LOCATION = configuration.getString(ConfigurationKeys.default_log_file_location);
    }

    private static void setupEndpointConfiguration() throws MalformedURLException, DecryptException {
        List<String> targetFailoverHosts = new ArrayList<String>();
        targetFailoverHosts.add(FAILOVER_HOST_1);
        Host soapEndpointHost = new Host();
        soapEndpointHost.setEndpoint(VTM_ENDPOINT_URI);
        soapEndpointHost.setRestEndpoint(VTM_ENDPOINT_URI);
        Host trafficManagerHost = new Host();
        trafficManagerHost.setEndpoint(VTM_ENDPOINT_URI);
        trafficManagerHost.setRestEndpoint(VTM_ENDPOINT_URI);
        trafficManagerHost.setTrafficManagerName(TARGET_HOST);
        List<Host> failoverHosts = new ArrayList<Host>();
        failoverHosts.add(soapEndpointHost);
        config = new LoadBalancerEndpointConfiguration(soapEndpointHost, VTM_USERNAME, CryptoUtil.decrypt(VTM_PASSWORD), trafficManagerHost, targetFailoverHosts, failoverHosts);
        config.setLogFileLocation(DEFAULT_LOG_FILE_LOCATION);
    }

    private static void setUpClusterForIPv6Operations() {
        cluster = new Cluster();
        cluster.setClusterIpv6Cidr("fd24:f480:ce44:91bc::/64");
        config.getTrafficManagerHost().setCluster(cluster);
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
        node1.setStatus(NodeStatus.ONLINE);
        node2.setStatus(NodeStatus.ONLINE);
        node1.setWeight(1);
        node2.setWeight(1);
        nodeList.add(node1);
        nodeList.add(node2);

        LoadBalancer lb = new LoadBalancer();
        lb.setId(TEST_LOADBALANCER_ID);
        lb.setAccountId(TEST_ACCOUNT_ID);
        lb.setPort(80);
        lb.setAlgorithm(ROUND_ROBIN);
        lb.setName("VTM-TESTER");
        lb.setProtocol(HTTP);
        lb.setNodes(nodeList);
        lb.setLoadBalancerJoinVipSet(vipList);

        VTMTestBase.lb = lb;
    }

    protected static String loadBalancerName() throws InsufficientRequestException {
        return VTMNameBuilder.genVSName(lb);
    }

    protected static String secureLoadBalancerName() throws InsufficientRequestException {
        return VTMNameBuilder.genSslVSName(lb.getId(), lb.getAccountId());
    }

    protected static String poolName() throws InsufficientRequestException {
        return VTMNameBuilder.genVSName(lb);
    }

    protected static String protectionClassName() throws InsufficientRequestException {
        return VTMNameBuilder.genVSName(lb);
    }

    protected static String secureProtectionClassName() throws InsufficientRequestException {
        return VTMNameBuilder.genSslVSName(lb);
    }

    protected static String trafficIpGroupName(VirtualIp vip) throws InsufficientRequestException {
        return VTMNameBuilder.generateTrafficIpGroupName(lb, vip);
    }

    protected static String trafficIpGroupName(VirtualIpv6 ipv6Vip) throws InsufficientRequestException {
        return VTMNameBuilder.generateTrafficIpGroupName(lb, ipv6Vip);
    }

    protected static String rateLimitName() throws InsufficientRequestException {
        return VTMNameBuilder.genVSName(lb);
    }

    protected static String errorFileName() throws InsufficientRequestException {
        return VTMNameBuilder.generateErrorPageName(lb.getId(), lb.getAccountId());
    }

    protected static String monitorName() throws InsufficientRequestException {
        return VTMNameBuilder.genVSName(lb);
    }

    protected static void createSimpleLoadBalancer() {
        VTMRestClient tclient;
        VTMResourceTranslator translator = new VTMResourceTranslator();
        try {
            vtmAdapter.createLoadBalancer(config, lb);
            VirtualServer vs;
            Pool pool;

            vs = vtmClient.getVirtualServer(loadBalancerName());

            Assert.assertNotNull(vs);
            Assert.assertEquals(true, vs.getProperties().getBasic().getEnabled());
            Assert.assertEquals(lb.getPort(), vs.getProperties().getBasic().getPort());
            Assert.assertEquals(poolName(), vs.getProperties().getBasic().getPool());
            Assert.assertEquals("Default", vs.getProperties().getConnectionErrors().getErrorFile());
            Assert.assertTrue(vs.getProperties().getHttp().getAddXForwardedFor());
            Assert.assertTrue(vs.getProperties().getHttp().getAddXForwardedProto());
            Assert.assertEquals(false, vs.getProperties().getBasic().getListenOnAny());
            Assert.assertEquals(false, vs.getProperties().getTcp().getProxyClose());
            Assert.assertEquals(translator.genGroupNameSet(lb), vs.getProperties().getBasic().getListenOnTrafficIps());

            Assert.assertEquals("", vs.getProperties().getBasic().getProtectionClass());
            Assert.assertEquals("", vs.getProperties().getBasic().getBandwidthClass());

            pool = vtmClient.getPool(loadBalancerName());
            Assert.assertNotNull(pool);
            Assert.assertEquals(0, pool.getProperties().getBasic().getMonitors().size());
            Assert.assertEquals(PoolLoadbalancing.Algorithm.fromValue(lb.getAlgorithm().name().toLowerCase()), pool.getProperties().getLoadBalancing().getAlgorithm());

            TrafficIp vip;
            for (String v : vs.getProperties().getBasic().getListenOnTrafficIps()) {
                vip = vtmClient.getTrafficIp(v);
                Assert.assertNotNull(vip);
                Assert.assertEquals(1, vip.getProperties().getBasic().getIpaddresses().size());
                Assert.assertEquals(true, vip.getProperties().getBasic().getEnabled());
                Assert.assertEquals(new HashSet(Arrays.asList(lb.getLoadBalancerJoinVipSet().iterator().next().getVirtualIp().getIpAddress())), vip.getProperties().getBasic().getIpaddresses());
                Set<String> machines = new HashSet<String>();
                machines.add(config.getTrafficManagerName());
                machines.addAll(config.getFailoverTrafficManagerNames());
                Assert.assertEquals(machines, vip.getProperties().getBasic().getMachines());
                Assert.assertEquals(new HashSet(config.getFailoverTrafficManagerNames()), vip.getProperties().getBasic().getSlaves());
            }

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());

        }
    }

    protected static void removeLoadBalancer() {
        try {
            vtmAdapter.deleteLoadBalancer(config, lb);
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        try {
            vtmClient.getVirtualServer(loadBalancerName());
            Assert.fail("Virtual Server should have been deleted!");
        } catch (Exception e) {
            if (e instanceof VTMRestClientObjectNotFoundException) {
            } else {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }

        try {
            vtmClient.getVirtualServer(secureLoadBalancerName());
            Assert.fail("Secure virtual Server should have been deleted!");
        } catch (Exception e) {
            if (e instanceof VTMRestClientObjectNotFoundException) {
            } else {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }

        try {
            vtmClient.getPool(poolName());
            Assert.fail("Node Pool should have been deleted!");
        } catch (Exception e) {
            if (e instanceof VTMRestClientObjectNotFoundException) {
            } else {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }

        try {
            if (lb.getHealthMonitor() != null) {
                vtmClient.getMonitor(monitorName());
                Assert.fail("Health monitor should have been deleted!");
            }
        } catch (Exception e) {
            if (e instanceof VTMRestClientObjectNotFoundException) {
            } else {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }

        try {
            if (lb.getRateLimit() != null) {
                vtmClient.getBandwidth(rateLimitName());
                Assert.fail("Rate limit/Bandwidth should have been deleted!");
            }
        } catch (Exception e) {
            if (e instanceof VTMRestClientObjectNotFoundException) {
            } else {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }

        try {
            if (!lb.getAccessLists().isEmpty() || lb.getConnectionLimit() != null) {
                vtmClient.getProtection(protectionClassName());
                Assert.fail("Protection class should have been deleted!");
            }
        } catch (Exception e) {
            if (e instanceof VTMRestClientObjectNotFoundException) {
            } else {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }

        try {
            if (!lb.getLoadBalancerJoinVipSet().isEmpty()) {
                for (LoadBalancerJoinVip jv : lb.getLoadBalancerJoinVipSet()) {
                    vtmClient.getTrafficIp(trafficIpGroupName(jv.getVirtualIp()));

                }
                Assert.fail("Traffic ips should have been deleted!");
            }

            if (!lb.getLoadBalancerJoinVip6Set().isEmpty()) {
                for (LoadBalancerJoinVip6 jv : lb.getLoadBalancerJoinVip6Set()) {
                    vtmClient.getTrafficIp(trafficIpGroupName(jv.getVirtualIp()));
                }
                Assert.fail("Traffic ips should have been deleted!");
            }
        } catch (Exception e) {
            if (e instanceof VTMRestClientObjectNotFoundException) {
            } else {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }
        try {
            if (lb.getCertificateMappings() != null) {
                List<Child> kpl = vtmClient.getKeypairs();
                for (CertificateMapping cm : lb.getCertificateMappings()) {
                    String zn = VTMNameBuilder.generateCertificateName(lb.getId(), lb.getAccountId(), cm.getId());
                    Assert.assertFalse(kpl.contains(zn));
                }
            }
        } catch (Exception e) {
            if (e instanceof VTMRestClientObjectNotFoundException) {
            } else {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }
    }

    protected static void teardownEverything() {
        removeLoadBalancer();
        vtmClient.destroy();
    }
}
