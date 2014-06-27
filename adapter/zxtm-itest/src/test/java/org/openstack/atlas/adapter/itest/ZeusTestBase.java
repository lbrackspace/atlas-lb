package org.openstack.atlas.adapter.itest;

import Util.ConfigurationKeys;
import Util.ZxtmItestConfiguration;
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
import org.openstack.atlas.util.ip.IPUtils;
import org.xml.sax.SAXException;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.*;

import static org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm.ROUND_ROBIN;
import static org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.HTTP;
import static org.openstack.atlas.service.domain.entities.NodeCondition.DISABLED;
import static org.openstack.atlas.service.domain.entities.NodeCondition.ENABLED;

public class ZeusTestBase {
    private static Configuration configuration = new ZxtmItestConfiguration();
    public static final Integer SLEEP_TIME_BETWEEN_TESTS = 500;
    public static final Integer NUM_VIPS_TO_ADD = 20;

    public static String ZXTM_USERNAME;
    public static String ZXTM_PASSWORD;
    public static String ZXTM_ENDPOINT_URI;
    public static String ZXTM_REST_ENDPOINT_URI;
    public static String TARGET_HOST;
    public static String FAILOVER_HOST_1;
    public static String FAILOVER_HOST_2;
    public static String DEFAULT_LOG_FILE_LOCATION;
    public static String ZXTM_VERSION;

    public static Integer TEST_ACCOUNT_ID;
    public static Integer TEST_LOADBALANCER_ID;
    public static Integer TEST_VIP_ID;
    public static Integer TEST_IPV6_VIP_ID;
    public static Integer ADDITIONAL_VIP_ID;
    public static Integer ADDITIONAL_IPV6_VIP_ID;

    protected static Map<String, Boolean> suitableVips;

    protected static ReverseProxyLoadBalancerAdapter zxtmAdapter;
    protected static LoadBalancerEndpointConfiguration config;
    protected static LoadBalancer lb;
    protected static VirtualIp vip1;
    protected static Node node1;
    protected static Node node2;
    protected static Cluster cluster;

    final static String testCert = "-----BEGIN CERTIFICATE-----\n" +
            "MIIERTCCAy2gAwIBAgIJANISIu8YgMUjMA0GCSqGSIb3DQEBBQUAMHQxCzAJBgNV\n" +
            "BAYTAlVTMQ4wDAYDVQQIEwVUZXhhczEUMBIGA1UEBxMLU2FuIEFudG9uaW8xGjAY\n" +
            "BgNVBAoTEVJhY2tzcGFjZSBIb3N0aW5nMQ4wDAYDVQQLEwVMQmFhUzETMBEGA1UE\n" +
            "AxMKTEJhYVMgVGVhbTAeFw0xNDAzMTIyMjIxMjlaFw0yNzExMTkyMjIxMjlaMHQx\n" +
            "CzAJBgNVBAYTAlVTMQ4wDAYDVQQIEwVUZXhhczEUMBIGA1UEBxMLU2FuIEFudG9u\n" +
            "aW8xGjAYBgNVBAoTEVJhY2tzcGFjZSBIb3N0aW5nMQ4wDAYDVQQLEwVMQmFhUzET\n" +
            "MBEGA1UEAxMKTEJhYVMgVGVhbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoC\n" +
            "ggEBALFSup6Yre0NzS5VY5KYg4/kLiKGK4R0y+Sis4+gq7ULBWLpBC9WBCjZTP3b\n" +
            "/Infz5JnUNMAjbJPHevQmSoDuIaTejv29mlJf70HK1uZb93yPMeMC7tkYvdryX9O\n" +
            "EYmdycnNK4K3xsi8jSXJzCDI5xVe5kMsvu3DBigg4Gi81BjckeRPzIuHBEWp2scq\n" +
            "dZ2SNw4wNdSsLprPXMeVc0tD6NUcgbLu156b9xadkKggYHV/LmkeUbkmpONP+Sb9\n" +
            "jkb9vJovQBbtgCEY+paw3Wi0BgF52w5PG9CJhL1Tnv0AErIE0pszrZ96vWHo5aXk\n" +
            "PTxSJla9k8pkxelGqxkRwliHdXUCAwEAAaOB2TCB1jAdBgNVHQ4EFgQUiRdCLNFp\n" +
            "+3FUNos/8Vv8XaG3TXswgaYGA1UdIwSBnjCBm4AUiRdCLNFp+3FUNos/8Vv8XaG3\n" +
            "TXuheKR2MHQxCzAJBgNVBAYTAlVTMQ4wDAYDVQQIEwVUZXhhczEUMBIGA1UEBxML\n" +
            "U2FuIEFudG9uaW8xGjAYBgNVBAoTEVJhY2tzcGFjZSBIb3N0aW5nMQ4wDAYDVQQL\n" +
            "EwVMQmFhUzETMBEGA1UEAxMKTEJhYVMgVGVhbYIJANISIu8YgMUjMAwGA1UdEwQF\n" +
            "MAMBAf8wDQYJKoZIhvcNAQEFBQADggEBAEZjzMfvcBtXQHuaqH8X5exfyn6iqxmo\n" +
            "S2kOfSvQxm5NLuSsNr5/LaGZpogGyQsywvcwBwmshZWRpNVoujqpujS2RB98nXRf\n" +
            "b134c1klK4poS3tx2BR+81OZYZG5cPq9S3y/XOBSBpvucSRQwoagf1sQOLB4pU8v\n" +
            "jgf/2pxJQtjhj7M4gZD7q1qwfTp0M3AyNV9KaI/EZN2e8ZJcpyruUJNe0ZuBW2+Y\n" +
            "obj7e8ogJJsV2y+DLstjzgFCz2/8upArZ7pI1mYwJMukAPzE8BPntrqHLMweUT3P\n" +
            "iOCsagBr8I/zbc4m/TwYvkcwxhrkaLlxwEtW7TID+LFnb0/NoPTdjO0=\n" +
            "-----END CERTIFICATE-----\n";


    final static String testKey = "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIIEpAIBAAKCAQEAsVK6npit7Q3NLlVjkpiDj+QuIoYrhHTL5KKzj6CrtQsFYukE\n" +
            "L1YEKNlM/dv8id/PkmdQ0wCNsk8d69CZKgO4hpN6O/b2aUl/vQcrW5lv3fI8x4wL\n" +
            "u2Ri92vJf04RiZ3Jyc0rgrfGyLyNJcnMIMjnFV7mQyy+7cMGKCDgaLzUGNyR5E/M\n" +
            "i4cERanaxyp1nZI3DjA11Kwums9cx5VzS0Po1RyBsu7Xnpv3Fp2QqCBgdX8uaR5R\n" +
            "uSak40/5Jv2ORv28mi9AFu2AIRj6lrDdaLQGAXnbDk8b0ImEvVOe/QASsgTSmzOt\n" +
            "n3q9YejlpeQ9PFImVr2TymTF6UarGRHCWId1dQIDAQABAoIBACm7jrBEvqpL1T5S\n" +
            "WlzmCBCVY0Y8zYEe+92TbS8gYUj6jwn4TUPWuqPigHw+ifDo+7E5H4yJVM/iTuhw\n" +
            "75szxPnnO51hQh0Fb0rNpSaptepGWIeeLiSsO55/f6y2cuoweI1F/DeHiQE1XwLF\n" +
            "u4T7w2cELq0gms7aV1iaZDZCOqie3Dub7KAL76jwpG3ECQlWzF04TjQ5lZBdM7Fa\n" +
            "z3fbaJ497k5DoPbZMqGi2eR7P8NJAPjIpmaL3vls2vlmWwd/7D10AJUNoILb74jm\n" +
            "648YFo76yKS15jtHFvifSaxEg3gjmth7IuRF4SbL5AjFqhj1qo9yQKLep7pNv9Bx\n" +
            "0eYoqwECgYEA4r3h/4WGuXrnh36zJW860O7+pO3l8rm83wP1oGc8xCK74aBQP5zL\n" +
            "JHaJypeImisZg3OcKL5IBop76LZ/i5oCDozHvTRByFHYnkRU3oh6FDcIvPkDCB7o\n" +
            "qq8y6Q+gbTJlKzpSxoRnj1rkHOweDzNG/7QD/D/g2z5ZejW3xC6H3R8CgYEAyDRe\n" +
            "Qv/ATAn1F0r7LweShjAcqaf5DxmXNDpaw7Wj0OKZxyxYw6aPVm3LnZP1tmGe9UlE\n" +
            "CFRTX5Y98x+9Z+PFtYgW0EdZCVQXKLkGJUhD8SRxyaS5Tlz1hzSHtbxGbDFuecRd\n" +
            "Qv/XmrJapVQrT4TMa5ivw836tjQhVqCrNyCHRusCgYEAk9o793IrkuFI/rqouN1a\n" +
            "HgnqNMQIcQma1lXvomQPZNo9Z3gxO/nTIXjGizva0KUQIv6NMqg5sUI2YF44t2B6\n" +
            "vOAiEwdzadutBC8MpHucF3h3kzpRNsdo8nwCF6Wf9/SnsdN7TIXkPb+IBjAVvdWz\n" +
            "E2RgQOmqh2yVzjIfHac14wMCgYEAkgiA6WYcIlrxB/iNmBRx8KePgMEhjr4f6NzX\n" +
            "8AHCaE+h1AKpDK2lyGl2KI8Qn+Q9SrYShfDcj9DLh1gTlIA0auHFok8oxwErk2zC\n" +
            "6tb3mCH5Thh1go+UGPdcNlgLFkhISVHOpVxxLEoEjKwEm5BGfAV3z9+jjNwhpUq1\n" +
            "GRUFF9kCgYBu/b84bEmflvv0z412hiQuIjDrJWPLUENfJujs6RitU42KV78Momif\n" +
            "/qrCK1exgdMiXET3nXg7Ff2zi5O8QArM3ITaWOczukAXaAeTPKm9o59ubb4PsU9K\n" +
            "A8Lv1syLCAC54udcbBGG2gvv7KVwJZQhmwItdX0ev5oAY3DTbJwstg==\n" +
            "-----END RSA PRIVATE KEY-----";

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
        ZXTM_VERSION = configuration.getString(ConfigurationKeys.zxtm_version);

        TEST_ACCOUNT_ID = Integer.valueOf(configuration.getString(ConfigurationKeys.test_account_id));
        TEST_LOADBALANCER_ID = Integer.valueOf(configuration.getString(ConfigurationKeys.test_loadbalancer_id));
        TEST_VIP_ID = Integer.valueOf(configuration.getString(ConfigurationKeys.test_vip_id));
        TEST_IPV6_VIP_ID = Integer.valueOf(configuration.getString(ConfigurationKeys.test_ipv6_vip_id));
        ADDITIONAL_VIP_ID = Integer.valueOf(configuration.getString(ConfigurationKeys.additional_vip_id));
        ADDITIONAL_IPV6_VIP_ID = Integer.valueOf(configuration.getString(ConfigurationKeys.additional_ipv6_vip_id));
        ZXTM_REST_ENDPOINT_URI = "https://zeus-endpoint:9090/tm/2.0/config/active/";
    }

    private static void setupEndpointConfiguration() throws MalformedURLException {
        List<String> targetFailoverHosts = new ArrayList<String>();
        targetFailoverHosts.add(FAILOVER_HOST_1);
        if (!FAILOVER_HOST_1.equals(FAILOVER_HOST_2)) targetFailoverHosts.add(FAILOVER_HOST_2);
        Host soapEndpointHost = new Host();
        soapEndpointHost.setEndpoint(ZXTM_ENDPOINT_URI);
        soapEndpointHost.setRestEndpoint(ZXTM_REST_ENDPOINT_URI);
        Host trafficManagerHost = new Host();
        trafficManagerHost.setTrafficManagerName(TARGET_HOST);
        List<Host> failoverHosts = new ArrayList<Host>();
        failoverHosts.add(soapEndpointHost);
        config = new LoadBalancerEndpointConfiguration(soapEndpointHost, ZXTM_USERNAME, ZXTM_PASSWORD, trafficManagerHost, targetFailoverHosts, failoverHosts);
        config.setLogFileLocation(DEFAULT_LOG_FILE_LOCATION);
    }

    private static void setUpClusterForIPv6Operations() {
        try {
            TrafficIPGroupsSubnetMappingPerHost[] subnetMappings = getServiceStubs().getTrafficIpGroupBinding().getSubnetMappings(new String[]{TARGET_HOST});
            for (String s : subnetMappings[0].getSubnetmappings()[0].getSubnets()) {
                if (IPUtils.isValidIpv6Subnet(s)) {
                    cluster = new Cluster();
                    cluster.setClusterIpv6Cidr(s);
                    return;
                }
            }
        } catch (RemoteException e) {
            Assert.fail("IPv6 isn't properly setup!");
        }
    }

    protected static void setupIvars() {
        Set<LoadBalancerJoinVip> vipList = new HashSet<LoadBalancerJoinVip>();
        vip1 = new VirtualIp();
        vip1.setId(TEST_VIP_ID);
        vip1.setIpAddress(findUsableIPv4Vip());
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

    protected static String findUsableIPv4Vip() {
        if (suitableVips == null) {
            setupUsableVips();
        }

        for (String s : suitableVips.keySet()) {
            Boolean isAvailableForUse = suitableVips.get(s);
            if (isAvailableForUse) {
                isAvailableForUse = false;
                suitableVips.put(s, isAvailableForUse);
                return s;
            }
        }

        return null;
    }

    protected static String makeUsableVipAvailable(String ipAddress) {
        if (suitableVips.get(ipAddress) != null) {
            Boolean isAvailableForUse = suitableVips.get(ipAddress);
            if (!isAvailableForUse) {
                isAvailableForUse = true;
                suitableVips.put(ipAddress, isAvailableForUse);
                return ipAddress;
            }
        }

        return null;
    }

    private static void setupUsableVips() {
        final Integer octet_min = 100;
        final Integer octet_max = 240;

        try {
            suitableVips = new HashMap<String, Boolean>();
            TrafficIPGroupsSubnetMappingPerHost[] subnetMappings = getServiceStubs().getTrafficIpGroupBinding().getSubnetMappings(new String[]{TARGET_HOST});
            for (String s : subnetMappings[0].getSubnetmappings()[0].getSubnets()) {
                if (IPUtils.isValidIpv4Subnet(s)) {
                    String[] split = s.split("\\.");
                    Random r = new Random();
                    int lastOctetStart = octet_max - r.nextInt(octet_min);

                    for (int i  = lastOctetStart; i < NUM_VIPS_TO_ADD + lastOctetStart; i++) {
                        String sVip = split[0] + "." + split[1] + "." + split[2] + "." + String.valueOf(i);
                        suitableVips.put(sVip, true);
                    }
                }
            }
        } catch (RemoteException e) {
            Assert.fail("Couldn't get an IPv4 address to use!");
        }
    }

    protected static ZxtmServiceStubs getServiceStubs() throws AxisFault {
        return ZxtmServiceStubs.getServiceStubs(config.getEndpointUrl(), config.getUsername(), config.getPassword());
    }

    protected static String loadBalancerName() throws InsufficientRequestException {
        return ZxtmNameBuilder.genVSName(lb);
    }

    protected static String secureLoadBalancerName() throws InsufficientRequestException {
        return ZxtmNameBuilder.genSslVSName(lb);
    }

    protected static String redirectLoadBalancerName() throws InsufficientRequestException {
        return ZxtmNameBuilder.genRedirectVSName(lb);
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
        return ZxtmNameBuilder.generateErrorPageName(lb.getId(), lb.getAccountId());
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
        try {
            Assert.assertEquals(ZXTM_VERSION, getServiceStubs().getSystemMachineInfoBinding().getProductVersion());
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
            Assert.assertEquals(lb.getProtocol().name().toLowerCase(), virtualServerBasicInfos[0].getProtocol().getValue().toLowerCase());
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

            HashSet<String> expectedNodes = new HashSet<String>();

            final String[][] enabledNodes = getServiceStubs().getPoolBinding().getNodes(new String[]{poolName()});
            Assert.assertEquals(1, enabledNodes.length);
            for (Node n : lb.getNodes()) {
                if (n.getCondition().equals(NodeCondition.ENABLED)) {
                    expectedNodes.add(IpHelper.createZeusIpString(n.getIpAddress(), n.getPort()));
                }
            }
            for (String n : enabledNodes[0]) {
                if (!expectedNodes.remove(n)) {
                    Assert.fail("Unexpected Node '" + n + "' found in pool '" + poolName() + "'!");
                }
            }
            if (!expectedNodes.isEmpty()) {
                Assert.fail("Nodes not found in pool '" + poolName() + "': " + expectedNodes.toString());
            }

            final PoolWeightingsDefinition[][] enabledNodeWeights = getServiceStubs().getPoolBinding().getNodesWeightings(new String[]{poolName()}, enabledNodes);
            Assert.assertEquals(1, enabledNodeWeights.length);
            Assert.assertEquals(enabledNodes[0].length, enabledNodeWeights[0].length);
            final HashMap<String, Integer> enabledNodeWeightsMap = new HashMap<String, Integer>();
            for (PoolWeightingsDefinition p : enabledNodeWeights[0]) {
                enabledNodeWeightsMap.put(p.getNode(), p.getWeighting());
            }
            for (Node n : lb.getNodes()) {
                if (n.getCondition().equals(NodeCondition.ENABLED)) {
                    if (n.getWeight() == null)
                        Assert.assertEquals(1,enabledNodeWeightsMap.get(IpHelper.createZeusIpString(n.getIpAddress(), n.getPort())).intValue());
                    else
                        Assert.assertEquals(n.getWeight(),enabledNodeWeightsMap.get(IpHelper.createZeusIpString(n.getIpAddress(), n.getPort())));
                }
            }

            final String[][] disabledNodes = getServiceStubs().getPoolBinding().getDisabledNodes(new String[]{poolName()});
            Assert.assertEquals(1, disabledNodes.length);
            for (Node n : lb.getNodes()) {
                if (n.getCondition().equals(NodeCondition.DISABLED)) {
                    expectedNodes.add(IpHelper.createZeusIpString(n.getIpAddress(), n.getPort()));
                }
            }
            for (String n : disabledNodes[0]) {
                if (!expectedNodes.remove(n)) {
                    Assert.fail("Unexpected Node '" + n + "' found in pool '" + poolName() + "'!");
                }
            }
            if (!expectedNodes.isEmpty()) {
                Assert.fail("Nodes not found in pool '" + poolName() + "': " + expectedNodes.toString());
            }

            final PoolWeightingsDefinition[][] disabledNodeWeights = getServiceStubs().getPoolBinding().getNodesWeightings(new String[]{poolName()}, disabledNodes);
            Assert.assertEquals(1, disabledNodeWeights.length);
            Assert.assertEquals(disabledNodes[0].length, disabledNodeWeights[0].length);
            final HashMap<String, Integer> disabledNodeWeightsMap = new HashMap<String, Integer>();
            for (PoolWeightingsDefinition p : disabledNodeWeights[0]) {
                disabledNodeWeightsMap.put(p.getNode(), p.getWeighting());
            }
            for (Node n : lb.getNodes()) {
                if (n.getCondition().equals(NodeCondition.DISABLED)) {
                    if (n.getWeight() == null)
                        Assert.assertEquals(1,disabledNodeWeightsMap.get(IpHelper.createZeusIpString(n.getIpAddress(), n.getPort())).intValue());
                    else
                        Assert.assertEquals(n.getWeight(),disabledNodeWeightsMap.get(IpHelper.createZeusIpString(n.getIpAddress(), n.getPort())));
                }
            }

            final String[][] drainingNodes = getServiceStubs().getPoolBinding().getDrainingNodes(new String[]{poolName()});
            Assert.assertEquals(1, drainingNodes.length);
            for (Node n : lb.getNodes()) {
                if (n.getCondition().equals(NodeCondition.DRAINING)) {
                    expectedNodes.add(IpHelper.createZeusIpString(n.getIpAddress(), n.getPort()));
                }
            }
            for (String n : drainingNodes[0]) {
                if (!expectedNodes.remove(n)) {
                    Assert.fail("Unexpected Node '" + n + "' found in pool '" + poolName() + "'!");
                }
            }
            if (!expectedNodes.isEmpty()) {
                Assert.fail("Nodes not found in pool '" + poolName() + "': " + expectedNodes.toString());
            }

            final PoolWeightingsDefinition[][] drainingNodeWeights = getServiceStubs().getPoolBinding().getNodesWeightings(new String[]{poolName()}, drainingNodes);
            Assert.assertEquals(1, drainingNodeWeights.length);
            Assert.assertEquals(drainingNodes[0].length, drainingNodeWeights[0].length);
            final HashMap<String, Integer> drainingNodeWeightsMap = new HashMap<String, Integer>();
            for (PoolWeightingsDefinition p : drainingNodeWeights[0]) {
                drainingNodeWeightsMap.put(p.getNode(), p.getWeighting());
            }
            for (Node n : lb.getNodes()) {
                if (n.getCondition().equals(NodeCondition.DRAINING)) {
                    if (n.getWeight() == null)
                        Assert.assertEquals(1,drainingNodeWeightsMap.get(IpHelper.createZeusIpString(n.getIpAddress(), n.getPort())).intValue());
                    else
                        Assert.assertEquals(n.getWeight(),drainingNodeWeightsMap.get(IpHelper.createZeusIpString(n.getIpAddress(), n.getPort())));
                }
            }

            final PoolLoadBalancingAlgorithm[] algorithms = getServiceStubs().getPoolBinding().getLoadBalancingAlgorithm(new String[]{poolName()});
            Assert.assertEquals(1, algorithms.length);
            Assert.assertEquals(PoolLoadBalancingAlgorithm.roundrobin.toString(), algorithms[0].getValue());

            final VirtualServerRule[][] virtualServerRules = getServiceStubs().getVirtualServerBinding().getRules(new String[]{loadBalancerName()});
            if (lb.getProtocol().name().toLowerCase().equals("http")) {
                Assert.assertEquals(1, virtualServerRules.length);
                Assert.assertEquals(1, virtualServerRules[0].length);
                Assert.assertEquals(ZxtmAdapterImpl.ruleXForwardedPort, virtualServerRules[0][0]);
            } else {
                Assert.assertEquals(1, virtualServerRules.length);
                Assert.assertEquals(0, virtualServerRules[0].length);
            }

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

        VirtualIp vip = lb.getLoadBalancerJoinVipSet().iterator().next().getVirtualIp();
        assertTrafficGroupIsDeleted(vip);
        makeUsableVipAvailable(vip.getIpAddress());
    }

    protected static void assertTrafficGroupIsDeleted(VirtualIp vip) {
        try {
            String trafficIpGroupName = trafficIpGroupName(vip);
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
