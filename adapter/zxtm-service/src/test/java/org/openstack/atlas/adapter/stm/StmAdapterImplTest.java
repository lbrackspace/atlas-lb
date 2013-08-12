package org.openstack.atlas.adapter.stm;

import org.junit.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.helpers.ResourceTranslator;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerStmAdapter;
import org.openstack.atlas.service.domain.entities.*;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.monitor.Monitor;
import org.rackspace.stingray.client.pool.Pool;
import org.rackspace.stingray.client.protection.Protection;
import org.rackspace.stingray.client.traffic.ip.TrafficIp;
import org.rackspace.stingray.client.virtualserver.VirtualServer;

import java.net.URI;
import java.util.*;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class StmAdapterImplTest {
    public static class WhenModifyingLoadbalancerResources {

        private ReverseProxyLoadBalancerStmAdapter stmAdapter;
        private StmAdapterResources resources;
        private LoadBalancer loadBalancer;
        private StingrayRestClient client;
        private LoadBalancerEndpointConfiguration configuration;
        private ResourceTranslator translator;

        @Before
        public void standUp () {
            stmAdapter = new StmAdapterImpl();
            resources = mock(StmAdapterResources.class);
            client = mock(StingrayRestClient.class);
            configuration = mock(LoadBalancerEndpointConfiguration.class);
            translator = mock(ResourceTranslator.class);
            loadBalancer = new LoadBalancer();
            loadBalancer.setPort(80);
            loadBalancer.setIpv6Public("::");
            loadBalancer.setIpv4Public("10.0.0.1");
            Set<AccessList> accessLists = new HashSet<AccessList>();
            AccessList item = new AccessList();
            item.setUserName("username");
            item.setId(0);
            item.setIpAddress("10.0.0.2");
            item.setIpVersion(IpVersion.IPV4);
            item.setType(AccessListType.DENY);
            item.setLoadbalancer(loadBalancer);
            accessLists.add(item);
            loadBalancer.setAccessLists(accessLists);
            loadBalancer.setAccountId(13531);
            loadBalancer.setAlgorithm(LoadBalancerAlgorithm.ROUND_ROBIN);
            ConnectionLimit limit = new ConnectionLimit();
            limit.setId(0);
            limit.setUserName("username");
            limit.setLoadBalancer(loadBalancer);
            limit.setMaxConnectionRate(10);
            limit.setMaxConnections(10);
            limit.setMinConnections(1);
            limit.setRateInterval(3);
            loadBalancer.setConnectionLimit(limit);
            loadBalancer.setConnectionLogging(true);
            loadBalancer.setContentCaching(true);
            loadBalancer.setCreated(Calendar.getInstance());
            loadBalancer.setUpdated(Calendar.getInstance());
            loadBalancer.setHalfClosed(true);
            HealthMonitor monitor = new HealthMonitor();
            monitor.setUserName("username");
            monitor.setId(0);
            monitor.setAttemptsBeforeDeactivation(10);
            monitor.setBodyRegex("regex");
            monitor.setDelay(1);
            monitor.setHostHeader("header");
            monitor.setLoadbalancer(loadBalancer);
            monitor.setStatusRegex("regex");
            monitor.setPath("path");
            monitor.setTimeout(10);
            monitor.setType(HealthMonitorType.CONNECT);
            loadBalancer.setHealthMonitor(monitor);
            loadBalancer.setHost(new Host());
            loadBalancer.setName("loadbalancer");
            Set<Node> nodes = new HashSet<Node>();
            Node node = new Node();
            node.setPort(80);
            node.setLoadbalancer(loadBalancer);
            node.setCondition(NodeCondition.ENABLED);
            node.setIpAddress("10.0.1.0");
            List<NodeMeta> nodeMetadata = new ArrayList<NodeMeta>();
            NodeMeta nodeMeta = new NodeMeta();
            nodeMeta.setKey("color");
            nodeMeta.setNode(node);
            nodeMeta.setValue("red");
            nodeMeta.setId(0);
            nodeMeta.setUserName("username");
            nodeMetadata.add(nodeMeta);
            node.setNodeMetadata(nodeMetadata);
            node.setStatus(NodeStatus.ONLINE);
            node.setType(NodeType.PRIMARY);
            node.setWeight(10);
            nodes.add(node);
            loadBalancer.setNodes(nodes);
            Set<LoadbalancerMeta> lbMetadata = new HashSet<LoadbalancerMeta>();
            LoadbalancerMeta lbMeta = new LoadbalancerMeta();
            lbMeta.setUserName("username");
            lbMeta.setId(0);
            lbMeta.setKey("color");
            lbMeta.setValue("red");
            lbMeta.setLoadbalancer(loadBalancer);
            lbMetadata.add(lbMeta);
            loadBalancer.setLoadbalancerMetadata(lbMetadata);
            loadBalancer.setProtocol(LoadBalancerProtocol.HTTP);
            RateLimit limits = new RateLimit();
            limits.setLoadbalancer(loadBalancer);
            limits.setId(0);
            limits.setUserName("username");
            limits.setExpirationTime(Calendar.getInstance());
            limits.setMaxRequestsPerSecond(3);
            Ticket ticket = new Ticket();
            ticket.setUserName("username");
            ticket.setId(0);
            ticket.setLoadbalancer(loadBalancer);
            ticket.setComment("Comment");
            ticket.setTicketId("1234321");
            limits.setTicket(ticket);
            loadBalancer.setRateLimit(limits);
            //Todo: Real cert insertion here
            loadBalancer.setSessionPersistence(SessionPersistence.HTTP_COOKIE);
            SslTermination termination = new SslTermination();
            termination.setSecurePort(443);
            termination.setCertificate("certificate");
            termination.setLoadbalancer(loadBalancer);
            termination.setIntermediateCertificate("interCert");
            termination.setPrivatekey("keyInPrivate");
            termination.setSecureTrafficOnly(false);
            termination.setId(0);
            termination.setUserName("username");
            termination.setEnabled(true);
            loadBalancer.setSslTermination(termination);
            loadBalancer.setStatus(LoadBalancerStatus.ACTIVE);
            loadBalancer.setSticky(false);
            Suspension suspension = new Suspension();
            suspension.setUserName("username");
            suspension.setId(0);
            suspension.setLoadbalancer(loadBalancer);
            suspension.setUser("user");
            suspension.setReason("because");
            suspension.setTicket(ticket);
            loadBalancer.setSuspension(suspension);
            Set<Ticket> tickets = new HashSet<Ticket>();
            tickets.add(ticket);
            loadBalancer.setTickets(tickets);
            loadBalancer.setTimeout(10);
            UserPages pages = new UserPages();
            pages.setLoadbalancer(loadBalancer);
            pages.setId(0);
            pages.setUserName("username");
            pages.setErrorpage("ERROR");
            loadBalancer.setUserPages(pages);
            loadBalancer.setId(0);
            loadBalancer.setUserName("username");
        }

        @Ignore
        @Test
        public void testCreateLoadBalancer() {
            try {
                doNothing().when(resources).updateHealthMonitor(Matchers.any(LoadBalancerEndpointConfiguration.class), Matchers.any(StingrayRestClient.class), Matchers.anyString(), Matchers.<Monitor>any());
                doNothing().when(resources).createPersistentClasses(Matchers.any(LoadBalancerEndpointConfiguration.class));
                doNothing().when(resources).updateProtection(Matchers.any(LoadBalancerEndpointConfiguration.class), Matchers.any(StingrayRestClient.class), Matchers.anyString(), Matchers.<Protection>any());
                doNothing().when(resources).updateVirtualIps(Matchers.any(LoadBalancerEndpointConfiguration.class), Matchers.any(StingrayRestClient.class), Matchers.anyString(), Matchers.anyMapOf(String.class, TrafficIp.class));
                doNothing().when(resources).updatePool(Matchers.any(LoadBalancerEndpointConfiguration.class), Matchers.any(StingrayRestClient.class), Matchers.anyString(), Matchers.<Pool>any());
                doNothing().when(resources).updateVirtualServer(Matchers.any(LoadBalancerEndpointConfiguration.class), Matchers.any(StingrayRestClient.class), Matchers.anyString(), Matchers.any(VirtualServer.class));
                doNothing().when(translator).translateLoadBalancerResource(Matchers.any(LoadBalancerEndpointConfiguration.class), Matchers.anyString(), Matchers.any(LoadBalancer.class), Matchers.any(LoadBalancer.class));
                when(configuration.getRestEndpoint()).thenReturn(URI.create("test"));
                when(configuration.getUsername()).thenReturn("testName");
                when(configuration.getPassword()).thenReturn("testPassword");
                when(resources.loadSTMRestClient(configuration)).thenReturn(client);
                stmAdapter.createLoadBalancer(configuration, loadBalancer);
            } catch (Exception e) {
                Assert.fail(e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
            }
        }

        @Test
        public void testUpdateLoadBalancer() {
        }

        @Test
        public void testDeleteLoadBalancer() {
        }

        @After
        public void tearDown() {
        }
    }

    @Ignore
    public static class VirtualServerResourcesTest {
        @Before
        public void standUp() {
        }

        @After
        public void tearDown() {
        }
    }

    @Ignore
    public static class PoolResourcesTest {
        @Before
        public void standUp() {
        }

        @After
        public void tearDown() {
        }
    }

    @Ignore
    public static class VirtualIpResourcesTest {
        @Before
        public void standUp() {
        }

        @After
        public void tearDown() {
        }
    }

    @Ignore
    public static class MonitorResourcesTest {
        @Before
        public void standUp() {
        }

        @After
        public void tearDown() {
        }
    }

    @Ignore
    public static class ProtectionResourcesTest {
        @Before
        public void standUp() {
        }

        @After
        public void tearDown() {
        }
    }

    @Ignore
    public static class PersistenceResourcesTest {
        @Before
        public void standUp() {
        }

        @After
        public void tearDown() {
        }
    }

    @Ignore
    public static class SslTerminationResourcesTest {
        @Before
        public void standUp() {
        }

        @After
        public void tearDown() {
        }
    }

    @Ignore
    public static class BandwidthResourcesTest {
        @Before
        public void standUp() {
        }

        @After
        public void tearDown() {
        }
    }

    @Ignore
    public static class ErrorFileResourcesTest {

        @Before
        public void standUp() {
        }

        @Test
        public void testUploadDefaultErrorFile() {
        }

        @Test
        public void testDeleteErrorFile() {
        }

        @After
        public void tearDown() {
        }
    }
}