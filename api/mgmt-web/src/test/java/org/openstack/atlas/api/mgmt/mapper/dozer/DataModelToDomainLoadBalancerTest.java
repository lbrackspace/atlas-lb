package org.openstack.atlas.api.mgmt.mapper.dozer;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.RateLimit;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Suspension;
import org.openstack.atlas.docs.loadbalancers.api.v1.*;
import org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm;
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entities.LoadBalancerProtocol;
import org.openstack.atlas.service.domain.entities.Ticket;
import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.*;

@RunWith(Enclosed.class)
public class DataModelToDomainLoadBalancerTest {

    private static final String managementDozerConfigFile = "loadbalancing-dozer-management-mapping.xml";

    public static class When_mapping_a_load_balancer_from_datamodel_to_domain {

        private DozerBeanMapper mapper;
        private LoadBalancer loadBalancer;
        private org.openstack.atlas.service.domain.entities.LoadBalancer domainLoadBalancer;

        @Before
        public void setUp() {
           mapper = MapperBuilder.getConfiguredMapper(managementDozerConfigFile);

            loadBalancer = new LoadBalancer();
            Created created = new Created();
            created.setTime(new GregorianCalendar(2010, Calendar.OCTOBER, 10));
            loadBalancer.setCreated(created);
            Updated updated = new Updated();
            updated.setTime(new GregorianCalendar(2010, Calendar.OCTOBER, 10));
            loadBalancer.setUpdated(updated);
            loadBalancer.setId(100);
            loadBalancer.setName("LB 1");
            loadBalancer.setPort(9999);

            loadBalancer.setProtocol("IMAPv4");
            loadBalancer.setAlgorithm("ROUND_ROBIN");
            loadBalancer.setStatus("SUSPENDED");

            org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket ticket = new org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket();
            ticket.setTicketId("1234");
            ticket.setComment("My first comment!");

            ConnectionLogging conLog = new ConnectionLogging();
            conLog.setEnabled(true);
            loadBalancer.setConnectionLogging(conLog);

            Node node1 = new Node();
            node1.setId(4100);
            node1.setAddress("10.1.1.1");
            node1.setPort(80);
            node1.setCondition(NodeCondition.DISABLED);

            Node node2 = new Node();
            node2.setId(4101);
            node2.setAddress("10.1.1.2");
            node2.setPort(85);
            node2.setCondition(NodeCondition.DRAINING);
            node2.setStatus(NodeStatus.OFFLINE);

            loadBalancer.getNodes().add(node1);
            loadBalancer.getNodes().add(node2);

            VirtualIp virtualIp1 = new VirtualIp();
            virtualIp1.setAddress("10.10.10.1");
            virtualIp1.setId(2000);
            virtualIp1.setType(VipType.PUBLIC);

            List<VirtualIp> virtualIps = new ArrayList<VirtualIp>();
            virtualIps.add(virtualIp1);
            loadBalancer.getVirtualIps().addAll(virtualIps);

            final ConnectionThrottle throttle = new ConnectionThrottle();
            throttle.setMaxConnectionRate(101);
            throttle.setMaxConnections(102);
            throttle.setMinConnections(100);
            throttle.setRateInterval(103);
            loadBalancer.setConnectionThrottle(throttle);

            final Suspension suspension = new Suspension();
            suspension.setReason("A new reason");
            suspension.setTicket(ticket);
            suspension.setUser("im a user");
            loadBalancer.setSuspension(suspension);

            final SessionPersistence sessionPersistence = new SessionPersistence();
            sessionPersistence.setPersistenceType(PersistenceType.HTTP_COOKIE);
            loadBalancer.setSessionPersistence(sessionPersistence);

            final HealthMonitor healthMonitor = new HealthMonitor();
            healthMonitor.setId(1);
            healthMonitor.setAttemptsBeforeDeactivation(1);
            healthMonitor.setDelay(1);
            healthMonitor.setTimeout(1);
            healthMonitor.setBodyRegex("some regex");
            healthMonitor.setStatusRegex("some regex");
            healthMonitor.setPath("some/path");
            healthMonitor.setType(HealthMonitorType.CONNECT);
            loadBalancer.setHealthMonitor(healthMonitor);

            final RateLimit rateLimit = new RateLimit();
            rateLimit.setTicket(ticket);
            rateLimit.setExpirationTime(Calendar.getInstance());
            rateLimit.setMaxRequestsPerSecond(10);
            loadBalancer.setRateLimit(rateLimit);

            loadBalancer.getTickets().add(ticket);

            domainLoadBalancer = mapper.map(loadBalancer,
                    org.openstack.atlas.service.domain.entities.LoadBalancer.class);
        }

        @Test
        public void should_not_fail_when_data_model_loadBalancer_is_empty() {
            loadBalancer = new LoadBalancer();
            try {
                domainLoadBalancer = mapper.map(loadBalancer,
                        org.openstack.atlas.service.domain.entities.LoadBalancer.class);
            } catch (Exception e) {
                Assert.fail("Empty domain load balancer caused this exception");
            }
        }

        @Test
        public void should_map_name_and_other_simple_types() {
            Assert.assertEquals(new Integer(100), domainLoadBalancer.getId());
            Assert.assertEquals("LB 1", domainLoadBalancer.getName());
            Assert.assertEquals(new Integer(9999), domainLoadBalancer.getPort());
            Assert.assertEquals(new GregorianCalendar(2010, Calendar.OCTOBER, 10),
                    domainLoadBalancer.getCreated());
            Assert.assertEquals(new GregorianCalendar(2010, Calendar.OCTOBER, 10),
                    domainLoadBalancer.getUpdated());
            Assert.assertEquals(true, domainLoadBalancer.isConnectionLogging());
        }

        @Test
        public void should_map_enumerations_on_the_loadbalancer() {
            Assert.assertEquals(LoadBalancerProtocol.IMAPv4,
                    domainLoadBalancer.getProtocol());
            Assert.assertEquals(LoadBalancerAlgorithm.ROUND_ROBIN,
                    domainLoadBalancer.getAlgorithm());
            Assert.assertEquals(
                    org.openstack.atlas.service.domain.entities.LoadBalancerStatus.SUSPENDED,
                    domainLoadBalancer.getStatus());
        }

        @Test
        public void should_map_the_node_list_across_the_two_load_balancers_and_the_properties_of_individual_nodes() {
            Assert.assertEquals(2, domainLoadBalancer.getNodes().size());

            for (org.openstack.atlas.service.domain.entities.Node node : domainLoadBalancer.getNodes()) {
                if (!(node.getId() == 4100 || node.getId() == 4101)) {
                    Assert.fail("Did not map the id of the node correctly");
                }
                if (!(node.getPort() == 80 || node.getPort() == 85)) {
                    Assert.fail("Did not map the port of the node correctly");
                }
                if (!(node.getIpAddress().equals("10.1.1.1") || node.getIpAddress().equals("10.1.1.2"))) {
                    Assert.fail("Did not map the ipAddress of the node correctly");
                }
                if (!(node.getCondition().equals(org.openstack.atlas.service.domain.entities.NodeCondition.DISABLED) || node.getCondition().equals(org.openstack.atlas.service.domain.entities.NodeCondition.DRAINING))) {
                    Assert.fail("Did not map the NodeCondition of the node correctly");
                }

                if (node.getStatus() == null) {
                    continue;
                }
                if (!node.getStatus().equals(org.openstack.atlas.service.domain.entities.NodeStatus.OFFLINE)) {
                    Assert.fail("Did not map the NodeStatus of the node correctly");
                }
            }
        }

        @Test
        public void should_map_the_virtual_ips_across_the_two_load_balancers_and_the_properties_of_individual_nodes() {
            Assert.assertEquals(1, domainLoadBalancer.getLoadBalancerJoinVipSet().size());

            for (LoadBalancerJoinVip loadBalancerJoinVip : domainLoadBalancer.getLoadBalancerJoinVipSet()) {
                Assert.assertEquals(new Integer(2000), loadBalancerJoinVip.getVirtualIp().getId());
                Assert.assertEquals("10.10.10.1", loadBalancerJoinVip.getVirtualIp().getIpAddress());
                Assert.assertEquals(
                        org.openstack.atlas.service.domain.entities.VirtualIpType.PUBLIC,
                        loadBalancerJoinVip.getVirtualIp().getVipType());
            }
        }

        @Test
        public void should_map_the_connection_limits_across_the_two_load_balancers() {
            Assert.assertEquals(new Integer(100), domainLoadBalancer.getConnectionLimit().getMinConnections());
            Assert.assertEquals(new Integer(101), domainLoadBalancer.getConnectionLimit().getMaxConnectionRate());
            Assert.assertEquals(new Integer(102), domainLoadBalancer.getConnectionLimit().getMaxConnections());
            Assert.assertEquals(new Integer(103), domainLoadBalancer.getConnectionLimit().getRateInterval());
        }

        @Test
        public void should_map_session_persistence_across_loadbalancers() {
            Assert.assertEquals(
                    org.openstack.atlas.service.domain.entities.SessionPersistence.HTTP_COOKIE,
                    domainLoadBalancer.getSessionPersistence());
        }

        @Test
        public void should_map_health_monitor_and_its_properties() {
            org.openstack.atlas.service.domain.entities.HealthMonitor healthMonitor = domainLoadBalancer.getHealthMonitor();
            Assert.assertEquals(null, healthMonitor.getId());
            Assert.assertEquals(new Integer(1),
                    healthMonitor.getAttemptsBeforeDeactivation());
            Assert.assertEquals(new Integer(1), healthMonitor.getDelay());
            Assert.assertEquals(new Integer(1), healthMonitor.getTimeout());
            Assert.assertEquals("some regex", healthMonitor.getBodyRegex());
            Assert.assertEquals("some regex", healthMonitor.getStatusRegex());
            Assert.assertEquals("some/path", healthMonitor.getPath());
            Assert.assertEquals(
                    org.openstack.atlas.service.domain.entities.HealthMonitorType.CONNECT,
                    healthMonitor.getType());
        }

        @Test
        public void shouldMapRateLimit() {
            org.openstack.atlas.service.domain.entities.RateLimit rateLimit = domainLoadBalancer.getRateLimit();
            Assert.assertEquals("1234", rateLimit.getTicket().getTicketId());
            Assert.assertNotNull(rateLimit.getExpirationTime());
            Assert.assertEquals(new Integer(10),
                    rateLimit.getMaxRequestsPerSecond());
        }

        @Test
        public void shouldMapLbSuspension() {
            org.openstack.atlas.service.domain.entities.Suspension suspension = domainLoadBalancer.getSuspension();
            Assert.assertEquals("1234", suspension.getTicket().getTicketId());
            Assert.assertEquals("A new reason", suspension.getReason());
            Assert.assertEquals("im a user", suspension.getUser());
        }

        @Test
        public void shouldNotMapTickets() {
            Assert.assertTrue(domainLoadBalancer.getTickets().isEmpty());
        }

        @Test
        public void shouldrHost2dHostTrue() {
            org.openstack.atlas.docs.loadbalancers.api.management.v1.Host rHost;
            org.openstack.atlas.service.domain.entities.Host dHost;
            rHost = new org.openstack.atlas.docs.loadbalancers.api.management.v1.Host();
            rHost.setEndpointActive(Boolean.TRUE);
            dHost = mapper.map(rHost, org.openstack.atlas.service.domain.entities.Host.class);
            Assert.assertEquals(Boolean.TRUE, dHost.isEndpointActive());
        }

        @Test
        public void shouldrHost2dHostActiveFalse() {
            org.openstack.atlas.docs.loadbalancers.api.management.v1.Host rHost;
            org.openstack.atlas.service.domain.entities.Host dHost;
            rHost = new org.openstack.atlas.docs.loadbalancers.api.management.v1.Host();
            rHost.setEndpointActive(Boolean.FALSE);
            dHost = mapper.map(rHost, org.openstack.atlas.service.domain.entities.Host.class);
            Assert.assertEquals(Boolean.FALSE, dHost.isEndpointActive());
        }

        @Test
        public void shouldrHost2dHostisActiveNull() {
            org.openstack.atlas.docs.loadbalancers.api.management.v1.Host rHost;
            org.openstack.atlas.service.domain.entities.Host dHost;
            rHost = new org.openstack.atlas.docs.loadbalancers.api.management.v1.Host();
            dHost = mapper.map(rHost, org.openstack.atlas.service.domain.entities.Host.class);
            Assert.assertEquals(null, dHost.isEndpointActive());
        }

        @Test
        public void shoulddHost2rHostTrue() {
            org.openstack.atlas.docs.loadbalancers.api.management.v1.Host rHost;
            org.openstack.atlas.service.domain.entities.Host dHost;
            dHost = new org.openstack.atlas.service.domain.entities.Host();
            dHost.setEndpointActive(Boolean.TRUE);
            rHost = mapper.map(dHost, org.openstack.atlas.docs.loadbalancers.api.management.v1.Host.class);
            Assert.assertEquals(Boolean.TRUE, rHost.isEndpointActive());
        }

        @Test
        public void shoulddHost2rHostActiveFalse() {
            org.openstack.atlas.docs.loadbalancers.api.management.v1.Host rHost;
            org.openstack.atlas.service.domain.entities.Host dHost;
            dHost = new org.openstack.atlas.service.domain.entities.Host();
            dHost.setEndpointActive(Boolean.FALSE);
            rHost = mapper.map(dHost, org.openstack.atlas.docs.loadbalancers.api.management.v1.Host.class);
            Assert.assertEquals(Boolean.FALSE, rHost.isEndpointActive());
        }

        @Test
        public void shoulddHost2rHostisActiveNull() {
            org.openstack.atlas.docs.loadbalancers.api.management.v1.Host rHost;
            org.openstack.atlas.service.domain.entities.Host dHost;
            dHost = new org.openstack.atlas.service.domain.entities.Host();
            rHost = mapper.map(dHost, org.openstack.atlas.docs.loadbalancers.api.management.v1.Host.class);
            Assert.assertEquals(null, rHost.isEndpointActive());
        }
    }

    public static class When_mapping_domain_to_datamodel {

        private DozerBeanMapper mapper;
        private LoadBalancer rLB;
        private org.openstack.atlas.service.domain.entities.LoadBalancer dLB;
        private LoadBalancer mLB;

        @Before
        public void setUp() {
            mapper = MapperBuilder.getConfiguredMapper(managementDozerConfigFile);

            dLB = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            rLB = new LoadBalancer();

            rLB.setId(1);
            dLB.setId(1);

            rLB.setStatus("ACTIVE");
            dLB.setStatus(org.openstack.atlas.service.domain.entities.LoadBalancerStatus.ACTIVE);

            rLB.setProtocol("HTTP");
            dLB.setProtocol(LoadBalancerProtocol.HTTP);

            rLB.setAlgorithm("ROUND_ROBIN");
            dLB.setAlgorithm(LoadBalancerAlgorithm.ROUND_ROBIN);

            rLB.setPort(80);
            dLB.setPort(80);

            Ticket ticket = new Ticket();
            ticket.setId(1);
            ticket.setTicketId("1234");
            ticket.setComment("Yay ticket!");
            ticket.setLoadbalancer(dLB);
            dLB.getTickets().add(ticket);
        }

        @Test
        public void shouldMapStatus() {
            mLB = mapper.map(dLB,
                    org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer.class);
            Assert.assertEquals(rLB.getStatus(), mLB.getStatus());
        }

        @Test
        public void shouldMapProtocol() {
            mLB = mapper.map(dLB,
                    org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer.class);
            Assert.assertEquals(rLB.getProtocol(), mLB.getProtocol());
        }

        @Test
        public void shouldMapAlgorithm() {
            mLB = mapper.map(dLB,
                    org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer.class);
            Assert.assertEquals(rLB.getAlgorithm(), mLB.getAlgorithm());
        }

        @Test
        public void shouldNotMapTickets() {
            mLB = mapper.map(dLB, org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer.class);
            Assert.assertNull(mLB.getTickets());
        }

        @Test
        public void shouldNotMapUsage() {
            mLB = mapper.map(dLB, org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer.class);
            Assert.assertNull(mLB.getLoadBalancerUsage());
        }

        @Test
        public void shouldNotMapAccessList() {
            mLB = mapper.map(dLB, org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer.class);
            Assert.assertNull(mLB.getAccessList());
        }
    }
}
