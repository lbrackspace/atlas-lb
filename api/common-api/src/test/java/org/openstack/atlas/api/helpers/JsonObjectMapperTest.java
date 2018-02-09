/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstack.atlas.api.helpers;

import org.junit.*;
import org.openstack.atlas.api.resources.StubResource;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.*;
import org.openstack.atlas.docs.loadbalancers.api.v1.*;
import org.openstack.atlas.docs.loadbalancers.api.v1.Errorpage;
import org.openstack.atlas.docs.loadbalancers.api.v1.IpVersion;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancers;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIps;
import org.openstack.atlas.util.debug.Debug;

import java.io.IOException;

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
        // TODO: Cafe tests will validate the outputs for the mostpart,
        // we should build these tests out a bit more here...

        StubResource stub = new StubResource();
        LoadBalancer loadBalancer = null;
        org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer loadBalancerman = null;
        String exMsg;
        LoadBalancer loadbalancer = (LoadBalancer) stub.stubLoadBalancer().getEntity();
        LoadBalancers loadbalancers = (LoadBalancers) stub.stubLoadBalancers().getEntity();

        loadBalancerman = new org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer();
        loadBalancerman.setId(1);
        loadBalancerman.setName("testsimplelbmgmt");
        loadBalancerman.setStatus("ACTIVE");
        loadBalancerman.setCreated(loadbalancer.getCreated());
        loadBalancerman.setNodes(loadbalancer.getNodes());
        org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancers loadbalancersman = new org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancers();
        loadbalancersman.getLoadBalancers().add(loadBalancerman);

        // Validate loadbalancers is mapped, we don't want root tags on the children
        String lbsStr = mapper.writeValueAsString(loadbalancers);
        // Very rough asserts...
        Assert.assertFalse(lbsStr.contains("\"loadBalancer\""));
        Assert.assertFalse(lbsStr.contains("\"node\""));
        Assert.assertTrue(lbsStr.contains("\"loadBalancers\""));
        Assert.assertTrue(lbsStr.contains("LB1"));
        Assert.assertTrue(lbsStr.contains("LB2"));
        Assert.assertTrue(lbsStr.contains("\"address\" : \"127.0.0.20\""));

        // Ensure the management loadbalancer is mapped too
        String lbsmanStr = mapper.writeValueAsString(loadbalancersman);
        Assert.assertFalse(lbsmanStr.contains("\"loadBalancer\""));
        Assert.assertFalse(lbsmanStr.contains("\"node\""));
        Assert.assertTrue(lbsmanStr.contains("\"loadBalancers\""));
        Assert.assertTrue(lbsmanStr.contains("\"name\" : \"testsimplelbmgmt\""));
        Assert.assertTrue(lbsmanStr.contains("\"address\" : \"127.0.0.20\""));
        // We didn't add vips and we don't want to map empty arrays.
        Assert.assertFalse(lbsmanStr.contains("\"virtualIps\" : [ ]"));
    }

    @Test
    public void shouldMapEmptyLoadbalancersWithRoot() throws IOException {

        LoadBalancers loadbalancers = new LoadBalancers();
        org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancers loadbalancersman = new org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancers();

        // Validate empty loadbalancers are created with a root tag
        String lbsStr = mapper.writeValueAsString(loadbalancers);
        String lbsmanStr = mapper.writeValueAsString(loadbalancersman);

        String expected = "{\n  \"loadBalancers\" : [ ]\n}";
        Assert.assertEquals(lbsStr, expected);
        Assert.assertEquals(lbsmanStr, expected);
    }

    public void nop() {
    }
}
