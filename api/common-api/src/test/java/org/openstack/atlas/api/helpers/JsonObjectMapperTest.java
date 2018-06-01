/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstack.atlas.api.helpers;

import org.dozer.Mapper;
import org.junit.*;
import org.openstack.atlas.api.mapper.dozer.MapperBuilder;
import org.openstack.atlas.api.resources.StubResource;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Host;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.HostType;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Zone;
import org.openstack.atlas.docs.loadbalancers.api.v1.AccessList;
import org.openstack.atlas.docs.loadbalancers.api.v1.AllowedDomain;
import org.openstack.atlas.docs.loadbalancers.api.v1.*;
import org.openstack.atlas.docs.loadbalancers.api.v1.CertificateMapping;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitor;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitorType;
import org.openstack.atlas.docs.loadbalancers.api.v1.IpVersion;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeCondition;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeStatus;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeType;
import org.openstack.atlas.docs.loadbalancers.api.v1.SessionPersistence;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp;
import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.util.debug.Debug;

import java.io.IOException;
import java.util.*;

public class JsonObjectMapperTest {

    private JsonObjectMapper mapper;

    public JsonObjectMapperTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        mapper = new JsonObjectMapper();
        mapper.init();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void shouldMapConnectionThrottleDeserialize() throws IOException {
        String ctJson = "{\"connectionThrottle\":{\"maxConnectionRate\":100,\"minConnections\":300,\"rateInterval\":60,\"maxConnections\": 200}}";
        ConnectionThrottle ct = mapper.readValue(ctJson, ConnectionThrottle.class);
        Assert.assertEquals(new Integer(100), ct.getMaxConnectionRate());
        Assert.assertEquals(new Integer(300), ct.getMinConnections());
        Assert.assertEquals(new Integer(200), ct.getMaxConnections());
        Assert.assertEquals(new Integer(60), ct.getRateInterval());
    }

    @Test
    public void shouldMapNodeDeserialize() throws IOException {
        String nJson = "{\"node\":{\"status\":\"ONLINE\",\"weight\":1,\"id\":64,\"address\":\"127.0.0.1\",\"port\":80,\"condition\": \"ENABLED\"}}";
        Node node = mapper.readValue(nJson, Node.class);
        Assert.assertEquals(NodeStatus.ONLINE, node.getStatus());
        Assert.assertEquals(new Integer(1), node.getWeight());
        Assert.assertEquals(new Integer(64), node.getId());
        Assert.assertEquals("127.0.0.1", node.getAddress());
        Assert.assertEquals(new Integer(80), node.getPort());
        Assert.assertEquals(NodeCondition.ENABLED, node.getCondition());
    }

    @Test
    public void shouldMapHealthMonitorDeserialize() throws IOException {
        String hmJson = "{\"healthMonitor\": {\"attemptsBeforeDeactivation\": 10,\"bodyRegex\":\".*\",\"statusRegex\":\".*\",\"delay\":60,\"timeout\": 100,\"path\":\"/\",\"type\":\"HTTP\",\"id\":64}}";
        HealthMonitor hm = mapper.readValue(hmJson, HealthMonitor.class);
        Assert.assertEquals(new Integer(10), hm.getAttemptsBeforeDeactivation());
        Assert.assertEquals(".*", hm.getBodyRegex());
        Assert.assertEquals(".*", hm.getStatusRegex());
        Assert.assertEquals(new Integer(60), hm.getDelay());
        Assert.assertEquals(new Integer(100), hm.getTimeout());
        Assert.assertEquals("/", hm.getPath());
        Assert.assertEquals(HealthMonitorType.HTTP, hm.getType());
    }

    @Test
    public void shouldMapSessionPersistenceDeserialize() throws IOException {
        String spJson = "{\"sessionPersistence\":{\"persistenceType\":\"HTTP_COOKIE\"}}";
        SessionPersistence sp = mapper.readValue(spJson, SessionPersistence.class);
        Assert.assertEquals(PersistenceType.HTTP_COOKIE, sp.getPersistenceType());
    }

    @Test
    public void shouldMapConnectionLoggingDeserialize() throws IOException {
        String clJson = "{\"connectionLogging\":{\"enabled\": true}}";
        ConnectionLogging cl = mapper.readValue(clJson, ConnectionLogging.class);
        Assert.assertEquals(Boolean.TRUE, cl.getEnabled());
    }

    @Test
    public void shouldMapNodesDeserializeWith1Elements() throws IOException {
        String nsJson = "{\"nodes\":[{\"address\":\"127.0.0.1\",\"id\":64,\"port\":80,\"status\":\"ONLINE\",\"condition\":\"ENABLED\",\"weight\":1}]}";
        Nodes nodes;
        nodes = mapper.readValue(nsJson, Nodes.class);
        Node node1 = nodes.getNodes().get(0);
        Assert.assertEquals(1, nodes.getNodes().size());
        Assert.assertEquals(NodeCondition.ENABLED, node1.getCondition());
        Assert.assertEquals(NodeStatus.ONLINE, node1.getStatus());
        Assert.assertEquals(new Integer(1), node1.getWeight());
        Assert.assertEquals("127.0.0.1", node1.getAddress());
        Assert.assertEquals(new Integer(64), node1.getId());
        Assert.assertEquals(new Integer(80), node1.getPort());
    }

    @Test
    public void shouldMapErrorPageDeserialize() throws IOException {
        String expected = "<html>Buzzoff!!!</html>";
        String epJson = String.format("{\"errorpage\":{\"content\":\"%s\"}}\"", expected);
        Errorpage errorpage;
        errorpage = mapper.readValue(epJson, Errorpage.class);
        nop();
        Assert.assertEquals(expected, errorpage.getContent());
    }

    @Test
    public void shouldMapAccessListDeserializeWith2Elements() throws IOException {
        String alJson = "{\"accessList\":[{\"ipVersion\":\"IPV4\",\"type\": \"DENY\",\"id\":1,\"address\":\"10.0.0.0/8\"},{\"ipVersion\":\"IPV4\",\"type\":\"DENY\",\"id\":2,\"address\":\"192.168.0.0/24\"}]}";
        AccessList al = mapper.readValue(alJson, AccessList.class);
        Assert.assertEquals(2, al.getNetworkItems().size());
        NetworkItem ni1 = al.getNetworkItems().get(0);
        NetworkItem ni2 = al.getNetworkItems().get(1);

        Assert.assertEquals(IpVersion.IPV4, ni1.getIpVersion());
        Assert.assertEquals(IpVersion.IPV4, ni2.getIpVersion());

        Assert.assertEquals(new Integer(1), ni1.getId());
        Assert.assertEquals(new Integer(2), ni2.getId());

        Assert.assertEquals("10.0.0.0/8", ni1.getAddress());
        Assert.assertEquals("192.168.0.0/24", ni2.getAddress());

        Assert.assertEquals(NetworkItemType.DENY, ni1.getType());
        Assert.assertEquals(NetworkItemType.DENY, ni2.getType());
    }

    @Test
    public void shouldMapAccessListDeserializeWith1Elements() throws IOException {
        String alJson = "{\"accessList\":[{\"ipVersion\":\"IPV4\",\"type\": \"DENY\",\"id\":1,\"address\":\"10.0.0.0/8\"}]}";
        AccessList al = mapper.readValue(alJson, AccessList.class);
        Assert.assertEquals(1, al.getNetworkItems().size());
        NetworkItem ni1 = al.getNetworkItems().get(0);
        Assert.assertEquals(IpVersion.IPV4, ni1.getIpVersion());
        Assert.assertEquals(new Integer(1), ni1.getId());
        Assert.assertEquals("10.0.0.0/8", ni1.getAddress());
        Assert.assertEquals(NetworkItemType.DENY, ni1.getType());
    }

    @Test
    public void shouldMapNodesDeserializeWith2Elements() throws IOException {
        String nsJson = "{\"nodes\":[{\"address\":\"127.0.0.1\",\"id\":64,\"port\":80,\"status\":\"ONLINE\",\"condition\":\"ENABLED\",\"weight\":1},{\"address\":\"127.0.0.2\",\"id\":65,\"port\":443,\"status\":\"ONLINE\",\"condition\":\"ENABLED\",\"weight\":1}]}";
        Nodes nodes;
        nodes = mapper.readValue(nsJson, Nodes.class);
        Node node1 = nodes.getNodes().get(0);
        Node node2 = nodes.getNodes().get(1);

        Assert.assertEquals(2, nodes.getNodes().size());

        Assert.assertEquals(NodeCondition.ENABLED, node1.getCondition());
        Assert.assertEquals(NodeCondition.ENABLED, node2.getCondition());

        Assert.assertEquals(NodeStatus.ONLINE, node1.getStatus());
        Assert.assertEquals(NodeStatus.ONLINE, node2.getStatus());

        Assert.assertEquals(new Integer(1), node1.getWeight());
        Assert.assertEquals(new Integer(1), node2.getWeight());


        Assert.assertEquals("127.0.0.1", node1.getAddress());
        Assert.assertEquals("127.0.0.2", node2.getAddress());

        Assert.assertEquals(new Integer(64), node1.getId());
        Assert.assertEquals(new Integer(65), node2.getId());

        Assert.assertEquals(new Integer(80), node1.getPort());
        Assert.assertEquals(new Integer(443), node2.getPort());

    }

    @Test
    public void shouldMapVirtualIpsDeserializeWith2Elements() throws IOException {
        String nsJson = "{\n" +
                "\t\"virtualIps\": [{\n" +
                "\t\t\"address\": \"127.0.0.1\",\n" +
                "\t\t\"id\": 64,\n" +
                "\t\t\"ipVersion\": \"IPV4\",\n" +
                "\t\t\"type\": \"PUBLIC\"\n" +
                "\t}, {\n" +
                "\t\t\"address\": \"127.0.0.2\",\n" +
                "\t\t\"id\": 65,\n" +
                "\t\t\"ipVersion\": \"IPV6\",\n" +
                "\t\t\"type\": \"SERVICENET\"\n" +
                "\t}]\n" +
                "}";
        VirtualIps vips;
        vips = mapper.readValue(nsJson, VirtualIps.class);
        VirtualIp vip1 = vips.getVirtualIps().get(0);
        VirtualIp vip2 = vips.getVirtualIps().get(1);

        Assert.assertEquals(2, vips.getVirtualIps().size());

        Assert.assertEquals(IpVersion.IPV4, vip1.getIpVersion());
        Assert.assertEquals(IpVersion.IPV6, vip2.getIpVersion());

        Assert.assertEquals("127.0.0.1", vip1.getAddress());
        Assert.assertEquals("127.0.0.2", vip2.getAddress());

        Assert.assertEquals(new Integer(64), vip1.getId());
        Assert.assertEquals(new Integer(65), vip2.getId());

        Assert.assertEquals(VipType.PUBLIC, vip1.getType());
        Assert.assertEquals(VipType.SERVICENET, vip2.getType());
    }

    @Test
    public void shouldMapCertificateMappingsDeserializeWith2Elements() throws IOException {
        String nsJson = "{\n" +
                "\t\"certificateMappings\": [{\n" +
                "\t\t\"hostName\": \"host1\",\n" +
                "\t\t\"id\": 64,\n" +
                "\t\t\"privateKey\": \"privkey1\",\n" +
                "\t\t\"certificate\": \"imacert1\",\n" +
                "\t\t\"intermediateCertificate\": \"intercert1\"\n" +
                "\t}, {\n" +
                "\t\t\"hostName\": \"host2\",\n" +
                "\t\t\"id\": 65,\n" +
                "\t\t\"privateKey\": \"privkey2\",\n" +
                "\t\t\"certificate\": \"imacert2\",\n" +
                "\t\t\"intermediateCertificate\": \"intercert2\"\n" +
                "\t}]\n" +
                "}";
        CertificateMappings cms;
        cms = mapper.readValue(nsJson, CertificateMappings.class);
        CertificateMapping cm1 = cms.getCertificateMappings().get(0);
        CertificateMapping cm2 = cms.getCertificateMappings().get(1);


        Assert.assertEquals(2, cms.getCertificateMappings().size());

        Assert.assertEquals("host1", cm1.getHostName());
        Assert.assertEquals("host2", cm2.getHostName());

        Assert.assertEquals("imacert1", cm1.getCertificate());
        Assert.assertEquals("imacert2", cm2.getCertificate());

        Assert.assertEquals(new Integer(64), cm1.getId());
        Assert.assertEquals(new Integer(65), cm2.getId());

        Assert.assertEquals("intercert1", cm1.getIntermediateCertificate());
        Assert.assertEquals("intercert2", cm2.getIntermediateCertificate());

        Assert.assertEquals("privkey1", cm1.getPrivateKey());
        Assert.assertEquals("privkey2", cm2.getPrivateKey());
    }

    @Test
    public void shouldMapAllowedDomainsDeserializeWith2Elements() throws IOException {
        String nsJson = "{\n" +
                "\t\"allowedDomains\": [{\n" +
                "\t\t\"name\": \"domain1\"\n" +
                "\t}, {\n" +
                "\t\t\"name\": \"domain2\"\n" +
                "\t}]\n" +
                "}";
        AllowedDomains ads;
        ads = mapper.readValue(nsJson, AllowedDomains.class);
        AllowedDomain ad1 = ads.getAllowedDomains().get(0);
        AllowedDomain ad2 = ads.getAllowedDomains().get(1);

        Assert.assertEquals(2, ads.getAllowedDomains().size());

        Assert.assertEquals("domain1", ad1.getName());
        Assert.assertEquals("domain2", ad2.getName());
    }

    @Test
    public void shouldCreateHost() throws IOException {
        String input = "{\"host\": {\"name\": \"someName\",\"zone\": \"B\",\"type\": \"FAILOVER\",\"managementIp\": \"12.34.56.78\",\"trafficManagerName\": \"zues01.blah.blah\",\"clusterId\": 1,\"maxConcurrentConnections\": 5,\"coreDeviceId\": \"SomeCoreDevice\",\"managementSoapInterface\": \"https://SomeSoapNode.com:9090\",\"soapEndpointActive\": \"true\",\"managementRestInterface\": \"https://SomeRestNode.com:9070\",\"restEndpointActive\": \"true\",\"ipv4Servicenet\": \"10.2.2.80\",\"ipv4Public\": \"172.11.11.110\"}}";

        Host host;
        host = mapper.readValue(input, Host.class);
        Assert.assertEquals(host.getZone(), Zone.B);
        Assert.assertEquals(host.getName(), "someName");
        Assert.assertEquals(host.getType(), HostType.FAILOVER);
        Assert.assertEquals(host.getManagementIp(), "12.34.56.78");
        Assert.assertEquals(host.getTrafficManagerName(), "zues01.blah.blah");
        Assert.assertEquals(host.getMaxConcurrentConnections(), new Integer(5));
        Assert.assertEquals(host.getManagementSoapInterface(), "https://SomeSoapNode.com:9090");
        Assert.assertEquals(host.getManagementRestInterface(), "https://SomeRestNode.com:9070");
        Assert.assertEquals(host.getRestEndpointActive(), true);
        Assert.assertEquals(host.getIpv4Servicenet(), "10.2.2.80");
        Assert.assertEquals(host.getCoreDeviceId(), "SomeCoreDevice");
        Assert.assertEquals(host.getIpv4Public(), "172.11.11.110");
        Assert.assertEquals(host.getClusterId(), new Integer(1));
        nop();
    }

    @Test
    public void shouldMapLoadBalancerDeserialize() throws IOException {
        String lbJson = "{\"loadBalancer\":{\"protocol\":\"HTTP\",\"name\":\"a-new-loadbalancer\",\"virtualIps\":[{\"id\":2341}],\"accessList\":[],\"nodes\":[{\"port\":80,\"condition\":\"ENABLED\",\"address\":\"10.1.1.1\"},{\"port\":443,\"condition\":\"ENABLED\",\"address\":\"10.1.1.3\"}],\"port\":80}}";
        LoadBalancer lb = mapper.readValue(lbJson, LoadBalancer.class);
        Assert.assertEquals("a-new-loadbalancer", lb.getName());
        Assert.assertEquals(new Integer(80), lb.getPort());
        Assert.assertEquals(1, lb.getVirtualIps().size());
        Assert.assertEquals(new Integer(2341), lb.getVirtualIps().get(0).getId());
        Assert.assertEquals(2, lb.getNodes().size());

        Assert.assertEquals(new Integer(80), lb.getNodes().get(0).getPort());
        Assert.assertEquals(new Integer(443), lb.getNodes().get(1).getPort());

        Assert.assertEquals("10.1.1.1", lb.getNodes().get(0).getAddress());
        Assert.assertEquals("10.1.1.3", lb.getNodes().get(1).getAddress());
    }

    @Test
    public void shouldDoSomeThing() throws IOException {
        StubResource stub = new StubResource();
        LoadBalancer loadBalancer = null;
        String exMsg;
        try {
            loadBalancer = (LoadBalancer) stub.stubLoadBalancer().getEntity();
        } catch (Throwable th) {
            exMsg = Debug.getEST(th);
            System.out.printf("Error %s\n",exMsg);
            nop();
        }
        LoadBalancers loadbalancers = (LoadBalancers) stub.stubLoadBalancers().getEntity();
        Nodes nodes = (Nodes) stub.stubNodes().getEntity();
        AccessList accessList = (AccessList) stub.stubAccessList().getEntity();
        VirtualIps vips = (VirtualIps) stub.stubVirtualIps().getEntity();
        Node node = (Node) stub.stubNode().getEntity();
        ConnectionThrottle ct = (ConnectionThrottle) stub.stubConnectionThrottle().getEntity();

        String lbStr = mapper.writeValueAsString(loadBalancer);
        String vipsStr = mapper.writeValueAsString(vips);
        String nodeStr = mapper.writeValueAsString(node);
        String alStr = mapper.writeValueAsString(accessList);
        String nodesStr = mapper.writeValueAsString(nodes);
        String lbsStr = mapper.writeValueAsString(loadbalancers);
        String ctStr = mapper.writeValueAsString(ct);
        nop();
    }

    @Test
    public void shouldSerializeSimpleLoadBalancers() throws IOException {
        // TODO: Cafe tests will validate the outputs for the most part,
        // we should build these tests out a bit more here...

        StubResource stub = new StubResource();
        LoadBalancer loadBalancer = null;
        String exMsg;
        LoadBalancer loadbalancer = (LoadBalancer) stub.stubLoadBalancer().getEntity();
        LoadBalancers loadbalancers = (LoadBalancers) stub.stubLoadBalancers().getEntity();

        // Validate loadbalancers is mapped, we don't want root tags on the children
        String lbsStr = mapper.writeValueAsString(loadbalancers);
        // Very rough asserts...
        Assert.assertFalse(lbsStr.contains("\"loadBalancer\""));
        Assert.assertFalse(lbsStr.contains("\"node\""));
        Assert.assertTrue(lbsStr.contains("\"loadBalancers\""));
        Assert.assertTrue(lbsStr.contains("LB1"));
        Assert.assertTrue(lbsStr.contains("LB2"));
        Assert.assertTrue(lbsStr.contains("\"address\" : \"127.0.0.20\""));
    }

    @Test
    public void shouldSerializeSimpleLoadBalancerSingleElemArray() throws IOException {
        // TODO: Cafe tests will validate the outputs for the most part,
        // we should build these tests out a bit more here...

        StubResource stub = new StubResource();
        LoadBalancer loadbalancer = (LoadBalancer) stub.stubLoadBalancer().getEntity();

        // We want to verify single element arrays mapp appropriately
        loadbalancer.getVirtualIps().remove(0);
        // Validate loadbalancers is mapped, we don't want root tags on the children
        String lbsStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(loadbalancer);
        // Very rough asserts...
        Assert.assertFalse(lbsStr.contains("\"loadBalancers\""));
        Assert.assertTrue(lbsStr.contains("\"loadBalancer\""));
        Assert.assertFalse(lbsStr.contains("\"node\""));
        Assert.assertTrue(lbsStr.contains("\"virtualIps\" : [ {"));
        Assert.assertFalse(lbsStr.contains("\"virtualIp\""));
        Assert.assertTrue(lbsStr.contains("\"address\" : \"127.0.0.20\""));
    }

    @Test
    public void shouldSerializeSimpleLoadBalancer() throws IOException {
        // TODO: Cafe tests will validate the outputs for the most part,
        // we should build these tests out a bit more here...

        StubResource stub = new StubResource();
        LoadBalancer loadbalancer = (LoadBalancer) stub.stubLoadBalancer().getEntity();

        // Validate loadbalancers is mapped, we don't want root tags on the children
        String lbsStr = mapper.writeValueAsString(loadbalancer);
        // Very rough asserts...
        Assert.assertFalse(lbsStr.contains("\"loadBalancers\""));
        Assert.assertTrue(lbsStr.contains("\"loadBalancer\""));
        Assert.assertFalse(lbsStr.contains("\"node\""));
        Assert.assertTrue(lbsStr.contains("\"virtualIps\" : [ {"));
        Assert.assertFalse(lbsStr.contains("\"virtualIp\""));
        Assert.assertTrue(lbsStr.contains("\"address\" : \"127.0.0.20\""));
    }

    @Test
    public void shouldSerializeSimpleErrorPage() throws IOException {
        //TODO: rework all the tests...
        Errorpage ep = new Errorpage();
        ep.setContent("ErrorpageContent");

        String epstr = mapper.writeValueAsString(ep);
        Assert.assertEquals("{\n  \"errorpage\" : {\n    \"content\" : \"ErrorpageContent\"\n  }\n}", epstr);
    }

    @Test
    public void shouldSerializeSimpleNodes() throws IOException {
        Nodes nodes = new Nodes();
        Node node = new Node();
        node.setId(1);
        node.setType(NodeType.PRIMARY);
        node.setAddress("10.2.2.2");
        nodes.getNodes().add(node);

        Node node2 = new Node();
        node2.setId(2);
        node2.setType(NodeType.SECONDARY);
        node2.setAddress("10.2.2.4");
        nodes.getNodes().add(node2);

        String epstr = mapper.writeValueAsString(nodes);
        Assert.assertEquals("{\n" +
                "  \"nodes\" : [ {\n" +
                "    \"id\" : 1,\n" +
                "    \"address\" : \"10.2.2.2\",\n" +
                "    \"type\" : \"PRIMARY\"\n" +
                //"    \"metadata\" : [ ]\n" +
                "  }, {\n" +
                "    \"id\" : 2,\n" +
                "    \"address\" : \"10.2.2.4\",\n" +
                "    \"type\" : \"SECONDARY\"\n" +
                //"    \"metadata\" : [ ]\n" +
                "  } ]\n" +
                "}", epstr);
    }

    @Test
    public void shouldSerializeSimpleNode() throws IOException {
        Node node = new Node();
        node.setId(1);
        node.setType(NodeType.PRIMARY);
        node.setAddress("10.2.2.2");

        String epstr = mapper.writeValueAsString(node);
        Assert.assertEquals("{\n" +
                "  \"node\" : {\n" +
                "    \"id\" : 1,\n" +
                "    \"address\" : \"10.2.2.2\",\n" +
                "    \"type\" : \"PRIMARY\"\n" +
                "  }\n" +
                "}", epstr);
    }

    @Test
    public void shouldSerializeSimpleContentCaching() throws IOException {
        ContentCaching cc = new ContentCaching();
        cc.setEnabled(true);
        String epstr = mapper.writeValueAsString(cc);
        Assert.assertEquals("{\n" +
                "  \"contentCaching\" : {\n" +
                "    \"enabled\" : true\n" +
                "  }\n" +
                "}", epstr);
    }

    @Test
    public void shouldSerializeSimpleHealthMonitor() throws IOException {
        HealthMonitor hm = new HealthMonitor();
        hm.setId(1);
        hm.setBodyRegex("regex");
        hm.setType(HealthMonitorType.CONNECT);

        String epstr = mapper.writeValueAsString(hm);
        Assert.assertEquals("{\n" +
                "  \"healthMonitor\" : {\n" +
                "    \"id\" : 1,\n" +
                "    \"bodyRegex\" : \"regex\",\n" +
                "    \"type\" : \"CONNECT\"\n" +
                "  }\n" +
                "}", epstr);
    }

    @Test
    public void shouldSerializeSimpleConnectionLogging() throws IOException {
        ConnectionLogging cl = new ConnectionLogging();
        cl.setEnabled(true);

        String epstr = mapper.writeValueAsString(cl);
        Assert.assertEquals("{\n" +
                "  \"connectionLogging\" : {\n" +
                "    \"enabled\" : true\n" +
                "  }\n" +
                "}", epstr);
    }


    @Test
    public void shouldSerializeSimpleSessionPersistence() throws IOException {
            SessionPersistence sp = new SessionPersistence();
            sp.setPersistenceType(PersistenceType.HTTP_COOKIE);

            String epstr = mapper.writeValueAsString(sp);
            Assert.assertEquals("{\n" +
                    "  \"sessionPersistence\" : {\n" +
                    "    \"persistenceType\" : \"HTTP_COOKIE\"\n" +
                    "  }\n" +
                    "}", epstr);
    }

    @Test
    public void shouldSerializeSimpleVips() throws IOException {
        VirtualIps vips = new VirtualIps();
        VirtualIp vip = new VirtualIp();
        vip.setId(1);
        vip.setAddress("1.1.1.1");
        vip.setIpVersion(IpVersion.IPV4);
        vips.getVirtualIps().add(vip);

        String epstr = mapper.writeValueAsString(vips);
        Assert.assertEquals("{\n" +
                "  \"virtualIps\" : [ {\n" +
                "    \"id\" : 1,\n" +
                "    \"address\" : \"1.1.1.1\",\n" +
                "    \"ipVersion\" : \"IPV4\"\n" +
                "  } ]\n" +
                "}", epstr);
    }

    @Test
    public void shouldSerializeSimpleConnectionThrottle() throws IOException {
        ConnectionThrottle ct = new ConnectionThrottle();
        ct.setMaxConnectionRate(1);
        ct.setRateInterval(2);

        String epstr = mapper.writeValueAsString(ct);
        Assert.assertEquals("{\n" +
                "  \"connectionThrottle\" : {\n" +
                "    \"maxConnectionRate\" : 1,\n" +
                "    \"rateInterval\" : 2\n" +
                "  }\n" +
                "}", epstr);
    }

    @Test
    public void shouldSerializeSimpleAllowedDomains() throws IOException {
        AllowedDomains ads = new AllowedDomains();
        AllowedDomain ad = new AllowedDomain();
        ad.setName("domain1");
        ads.getAllowedDomains().add(ad);

        String epstr = mapper.writeValueAsString(ads);
        Assert.assertEquals("{\n" +
                "  \"allowedDomains\" : [ {\n" +
                "    \"name\" : \"domain1\"\n" +
                "  } ]\n" +
                "}", epstr);
    }

    @Test
    public void shouldSerializeSimpleCertificateMapping() throws IOException {
        CertificateMapping ct = new CertificateMapping();
        ct.setId(1);
        ct.setCertificate("imacert");

        String epstr = mapper.writeValueAsString(ct);
        Assert.assertEquals("{\n" +
                "  \"certificateMapping\" : {\n" +
                "    \"certificate\" : \"imacert\",\n" +
                "    \"id\" : 1\n" +
                "  }\n" +
                "}", epstr);
    }

    @Test
    public void shouldSerializeSimpleCertificateMappings() throws IOException {
        CertificateMappings cms = new CertificateMappings();
        CertificateMapping cm = new CertificateMapping();
        cm.setId(1);
        cm.setHostName("host1");
        cms.getCertificateMappings().add(cm);

        String epstr = mapper.writeValueAsString(cms);
        Assert.assertEquals("{\n" +
                "  \"certificateMappings\" : [ {\n" +
                "    \"id\" : 1,\n" +
                "    \"hostName\" : \"host1\"\n" +
                "  } ]\n" +
                "}", epstr);
    }

    @Test
    public void shouldSerializeSimpleSslTermination() throws IOException {
        SslTermination ct = new SslTermination();
        ct.setCipherProfile("Pro1");
        ct.setEnabled(false);
        ct.setSecurePort(22);

        String epstr = mapper.writeValueAsString(ct);
        Assert.assertEquals("{\n" +
                "  \"sslTermination\" : {\n" +
                "    \"enabled\" : false,\n" +
                "    \"securePort\" : 22,\n" +
                "    \"cipherProfile\" : \"Pro1\"\n" +
                "  }\n" +
                "}", epstr);
    }

    @Test
    public void shouldMapEmptyLoadbalancersWithRoot() throws IOException {

        LoadBalancers loadbalancers = new LoadBalancers();

        // Validate empty loadbalancers are created with a root tag
        String lbsStr = mapper.writeValueAsString(loadbalancers);

        String expected = "{\n  \"loadBalancers\" : [ ]\n}";
        Assert.assertEquals(lbsStr, expected);
    }

    public void nop() {
    }

    public static org.openstack.atlas.service.domain.entities.LoadBalancer createHydratedLoadbalancer() {
        org.openstack.atlas.service.domain.entities.LoadBalancer loadBalancer = new org.openstack.atlas.service.domain.entities.LoadBalancer();
        loadBalancer.setCreated(new GregorianCalendar(2018, 05, 01));
        loadBalancer.setUpdated(new GregorianCalendar(2018, 05, 01));
        loadBalancer.setProvisioned(new GregorianCalendar(2018, 05, 01));
        loadBalancer.setId(100);
        loadBalancer.setName("a-new-loadbalancer");
        loadBalancer.setPort(8080);
        loadBalancer.setHalfClosed(false);
        loadBalancer.setHttpsRedirect(false);
        loadBalancer.setTimeout(30);

        loadBalancer.setAlgorithm(LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN);
        loadBalancer.setStatus(LoadBalancerStatus.BUILD);
        loadBalancer.setProtocol(LoadBalancerProtocol.HTTP);
        loadBalancer.setConnectionLogging(false);
        loadBalancer.setContentCaching(false);

        final Set<LoadbalancerMeta> loadbalancerMetaData = new HashSet<LoadbalancerMeta>();
        final LoadbalancerMeta loadbalancerMeta1 = new LoadbalancerMeta();
        loadbalancerMeta1.setId(991);
        loadbalancerMeta1.setKey("metaKey1");
        loadbalancerMeta1.setValue("metaValue1");
        loadbalancerMetaData.add(loadbalancerMeta1);
        final LoadbalancerMeta loadbalancerMeta2 = new LoadbalancerMeta();
        loadbalancerMeta2.setId(992);
        loadbalancerMeta2.setKey("metaKey2");
        loadbalancerMeta2.setValue("metaValue2");
        loadbalancerMetaData.add(loadbalancerMeta2);
        loadBalancer.setLoadbalancerMetadata(loadbalancerMetaData);

        final Set<org.openstack.atlas.service.domain.entities.Node> hashSet = new HashSet<org.openstack.atlas.service.domain.entities.Node>();
        final org.openstack.atlas.service.domain.entities.Node node1 = new org.openstack.atlas.service.domain.entities.Node();
        node1.setCondition(org.openstack.atlas.service.domain.entities.NodeCondition.ENABLED);
        node1.setId(100);
        node1.setIpAddress("216.58.201.46");
        node1.setPort(80);
        node1.setWeight(2);
        node1.setStatus(org.openstack.atlas.service.domain.entities.NodeStatus.ONLINE);
        /*final org.openstack.atlas.service.domain.entities.Node node2 = new org.openstack.atlas.service.domain.entities.Node();
        node2.setCondition(org.openstack.atlas.service.domain.entities.NodeCondition.DISABLED);
        node2.setId(101);
        node2.setIpAddress("ip2");
        node2.setPort(1001);
        node2.setStatus(org.openstack.atlas.service.domain.entities.NodeStatus.OFFLINE);*/
        NodeMeta nm = new NodeMeta();
        nm.setKey("color");
        nm.setValue("Red");
        List<NodeMeta> nodeMetas = new ArrayList<NodeMeta>();
        nodeMetas.add(nm);
        node1.setNodeMetadata(nodeMetas);
        hashSet.add(node1);
        loadBalancer.setNodes(hashSet);

        final Set<org.openstack.atlas.service.domain.entities.VirtualIp> virtualIpSet = new HashSet<org.openstack.atlas.service.domain.entities.VirtualIp>();
        final org.openstack.atlas.service.domain.entities.VirtualIp virtualIp1 = new org.openstack.atlas.service.domain.entities.VirtualIp();
        virtualIp1.setId(100);
        virtualIp1.setIpAddress("ip1");
        virtualIp1.setVipType(VirtualIpType.PUBLIC);
        LoadBalancerJoinVip loadBalancerJoinVip1 = new LoadBalancerJoinVip();
        loadBalancerJoinVip1.setVirtualIp(virtualIp1);
        loadBalancer.getLoadBalancerJoinVipSet().add(loadBalancerJoinVip1);

        Cluster cluster = new Cluster();
        cluster.setId(1);
        cluster.setClusterIpv6Cidr("ffff:ffff:ffff:ffff::/64");
        final VirtualIpv6 virtualIp3 = new VirtualIpv6();
        virtualIp3.setId(9000001);
        virtualIp3.setAccountId(1234);
        virtualIp3.setVipOctets(1);
        virtualIp3.setCluster(cluster);
        org.openstack.atlas.service.domain.entities.Host h = new org.openstack.atlas.service.domain.entities.Host();
        h.setIpv4Public("Ipv4Public");
        h.setIpv6Public("Ipv6Public");
        h.setIpv4Servicenet("");
        h.setId(1);
        h.setName("z2.rackexp.org");
        loadBalancer.setHost(h);
        LoadBalancerJoinVip6 loadBalancerJoinVip3 = new LoadBalancerJoinVip6();
        loadBalancerJoinVip3.setVirtualIp(virtualIp3);
        loadBalancer.getLoadBalancerJoinVip6Set().add(loadBalancerJoinVip3);

        final ConnectionLimit limit = new ConnectionLimit();
        limit.setRateInterval(13);
        limit.setMaxConnectionRate(10);
        limit.setMaxConnections(11);
        limit.setMinConnections(12);
        loadBalancer.setConnectionLimit(limit);
        loadBalancer.setSessionPersistence(org.openstack.atlas.service.domain.entities.SessionPersistence.HTTP_COOKIE);


        final org.openstack.atlas.service.domain.entities.HealthMonitor healthMonitor = new org.openstack.atlas.service.domain.entities.HealthMonitor();

        healthMonitor.setId(1);
        healthMonitor.setAttemptsBeforeDeactivation(3);
        healthMonitor.setDelay(10);
        healthMonitor.setTimeout(10);
        healthMonitor.setBodyRegex(".*");
        healthMonitor.setStatusRegex("^[234][0-9][0-9]$");
        healthMonitor.setPath("/");
        healthMonitor.setType(org.openstack.atlas.service.domain.entities.HealthMonitorType.HTTP);

        loadBalancer.setHealthMonitor(healthMonitor);

        return loadBalancer;
    }
    @Test
    public void shouldSerializeLoadBalancer() throws IOException {
        final String publicDozerConfigFile = "loadbalancing-dozer-mapping.xml";
        org.openstack.atlas.service.domain.entities.LoadBalancer domainLb = createHydratedLoadbalancer();
        domainLb.setSessionPersistence(null);
        domainLb.setConnectionLimit(null);
        domainLb.setLoadbalancerMetadata(null);

        Mapper dozerMapper = MapperBuilder.getConfiguredMapper(publicDozerConfigFile);
        LoadBalancer loadbalancer = dozerMapper.map(domainLb,
                org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer.class);

        String lbsStr =mapper.writeValueAsString(loadbalancer);
        Assert.assertEquals("{\n" +
                        "  \"loadBalancer\" : {\n" +
                        "    \"healthMonitor\" : {\n" +
                        "      \"delay\" : 10,\n" +
                        "      \"timeout\" : 10,\n" +
                        "      \"attemptsBeforeDeactivation\" : 3,\n" +
                        "      \"path\" : \"/\",\n" +
                        "      \"statusRegex\" : \"^[234][0-9][0-9]$\",\n" +
                        "      \"bodyRegex\" : \".*\",\n" +
                        "      \"type\" : \"HTTP\"\n" +
                        "    },\n" +
                        "    \"cluster\" : {\n" +
                        "      \"name\" : \"z2.rackexp.org\"\n" +
                        "    },\n" +
                        "    \"created\" : {\n" +
                        "      \"time\" : \"2018-05-31T18:30:00Z\"\n" +
                        "    },\n" +
                        "    \"updated\" : {\n" +
                        "      \"time\" : \"2018-05-31T18:30:00Z\"\n" +
                        "    },\n" +
                        "    \"connectionLogging\" : {\n" +
                        "      \"enabled\" : false\n" +
                        "    },\n" +
                        "    \"contentCaching\" : {\n" +
                        "      \"enabled\" : false\n" +
                        "    },\n" +
                        "    \"sourceAddresses\" : {\n" +
                        "      \"ipv4Public\" : \"Ipv4Public\",\n" +
                        "      \"ipv6Public\" : \"Ipv6Public\"\n" +
                        "    },\n" +
                        "    \"id\" : 100,\n" +
                        "    \"name\" : \"a-new-loadbalancer\",\n" +
                        "    \"algorithm\" : \"WEIGHTED_ROUND_ROBIN\",\n" +
                        "    \"protocol\" : \"HTTP\",\n" +
                        "    \"httpsRedirect\" : false,\n" +
                        "    \"halfClosed\" : false,\n" +
                        "    \"port\" : 8080,\n" +
                        "    \"status\" : \"BUILD\",\n" +
                        "    \"timeout\" : 30,\n" +
                        "    \"virtualIps\" : [ {\n" +
                        "      \"id\" : 100,\n" +
                        "      \"address\" : \"ip1\",\n" +
                        "      \"ipVersion\" : \"IPV4\",\n" +
                        "      \"type\" : \"PUBLIC\"\n" +
                        "    }, {\n" +
                        "      \"id\" : 9000001,\n" +
                        "      \"address\" : \"ffff:ffff:ffff:ffff:7110:eda4:0000:0001\",\n" +
                        "      \"ipVersion\" : \"IPV6\",\n" +
                        "      \"type\" : \"PUBLIC\"\n" +
                        "    } ],\n" +
                        "    \"nodes\" : [ {\n"+
                        "      \"id\" : 100,\n" +
                        "      \"address\" : \"216.58.201.46\",\n" +
                        "      \"port\" : 80,\n" +
                        "      \"condition\" : \"ENABLED\",\n" +
                        "      \"status\" : \"ONLINE\",\n" +
                        "      \"weight\" : 2,\n" +
                        "      \"type\" : \"PRIMARY\"\n" +
                        "    } ]\n" +
                        "  }\n" +
                "}", lbsStr);

        lbsStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(loadbalancer);
        //System.out.println(lbsStr);

    }
}
