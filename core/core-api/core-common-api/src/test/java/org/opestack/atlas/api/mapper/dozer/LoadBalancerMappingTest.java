package org.opestack.atlas.api.mapper.dozer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.core.api.v1.*;
import org.openstack.atlas.datamodel.HealthMonitorType;
import org.openstack.atlas.datamodel.NodeCondition;
import org.openstack.atlas.service.domain.entity.LoadBalancerAlgorithm;
import org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entity.LoadBalancerProtocol;
import org.openstack.atlas.service.domain.entity.NodeStatus;

import java.util.GregorianCalendar;

@RunWith(Enclosed.class)
public class LoadBalancerMappingTest {

    public static class WhenMappingALoadBalancerFromDataModelToDomain extends MappingBase {
        private LoadBalancer loadBalancer;
        private org.openstack.atlas.service.domain.entity.LoadBalancer domainLoadBalancer;

        @Before
        public void setUp() {
            loadBalancer = new LoadBalancer();

            Created created = new Created();
            Updated updated = new Updated();
            created.setTime(new GregorianCalendar(2010, 10, 10));
            updated.setTime(new GregorianCalendar(2010, 12, 10));
            loadBalancer.setCreated(created);
            loadBalancer.setUpdated(updated);

            loadBalancer.setId(100);
            loadBalancer.setName("LB 1");
            loadBalancer.setPort(9999);

            loadBalancer.setProtocol("IMAPv4");
            loadBalancer.setAlgorithm("ROUND_ROBIN");
            loadBalancer.setStatus("SUSPENDED");

            Node node1 = new Node();
            node1.setId(4100);
            node1.setAddress("10.1.1.1");
            node1.setPort(80);
            node1.setCondition(NodeCondition.ENABLED.name());

            Node node2 = new Node();
            node2.setId(4101);
            node2.setAddress("10.1.1.2");
            node2.setPort(85);
            node2.setCondition(NodeCondition.DISABLED.name());
            node2.setStatus(NodeStatus.OFFLINE.name());

            final Nodes nodes = new Nodes();
            nodes.getNodes().add(node1);
            nodes.getNodes().add(node2);
            loadBalancer.getNodes().addAll(nodes.getNodes());

            VirtualIp virtualIp1 = new VirtualIp();
            virtualIp1.setAddress("10.10.10.1");
            virtualIp1.setId(2000);
            virtualIp1.setType(VipType.PUBLIC);
            virtualIp1.setIpVersion(IpVersion.IPV4);

            loadBalancer.getVirtualIps().add(virtualIp1);

            final ConnectionThrottle throttle = new ConnectionThrottle();
            throttle.setMaxRequestRate(101);
            throttle.setRateInterval(103);
            loadBalancer.setConnectionThrottle(throttle);

            final HealthMonitor healthMonitor = new HealthMonitor();

            healthMonitor.setId(1);
            healthMonitor.setAttemptsBeforeDeactivation(1);
            healthMonitor.setDelay(1);
            healthMonitor.setTimeout(1);
            healthMonitor.setPath("some/path");
            healthMonitor.setType(HealthMonitorType.CONNECT.name());

            loadBalancer.setHealthMonitor(healthMonitor);

            domainLoadBalancer = mapper.map(loadBalancer, org.openstack.atlas.service.domain.entity.LoadBalancer.class);
        }

        @Test
        public void should_not_fail_when_data_model_loadBalancer_is_empty() {
            loadBalancer = new LoadBalancer();
            try {
                domainLoadBalancer = mapper.map(loadBalancer, org.openstack.atlas.service.domain.entity.LoadBalancer.class);
            } catch (Exception e) {
                Assert.fail("Empty domain load balancer caused this exception");
            }
        }

        @Test
        public void should_map_name_and_other_simple_types() {
            Assert.assertEquals(new Integer(100), domainLoadBalancer.getId());
            Assert.assertEquals("LB 1", domainLoadBalancer.getName());
            Assert.assertEquals(new Integer(9999), domainLoadBalancer.getPort());
            Assert.assertEquals(new GregorianCalendar(2010, 10, 10), domainLoadBalancer.getCreated());
            Assert.assertEquals(new GregorianCalendar(2010, 12, 10), domainLoadBalancer.getUpdated());
        }

        @Test
        public void should_map_enumerations_on_the_loadbalancer() {
            Assert.assertEquals(LoadBalancerProtocol.IMAPv4,
                    domainLoadBalancer.getProtocol());
            Assert.assertEquals(LoadBalancerAlgorithm.ROUND_ROBIN,
                    domainLoadBalancer.getAlgorithm());
            Assert.assertEquals(
                    org.openstack.atlas.service.domain.entity.LoadBalancerStatus.SUSPENDED,
                    domainLoadBalancer.getStatus());
        }

        @Test
        public void should_map_the_node_list_across_the_two_load_balancers_and_the_properties_of_individual_nodes() {
            Assert.assertEquals(2, domainLoadBalancer.getNodes().size());

            for (org.openstack.atlas.service.domain.entity.Node node : domainLoadBalancer
                    .getNodes()) {
                if (!(node.getId() == 4100 || node.getId() == 4101))
                    Assert.fail("Did not map the id of the node correctly");
                if (!(node.getPort() == 80 || node.getPort() == 85))
                    Assert.fail("Did not map the port of the node correctly");
                if (!(node.getIpAddress().equals("10.1.1.1") || node
                        .getIpAddress().equals("10.1.1.2")))
                    Assert.fail("Did not map the ipAddress of the node correctly");
                if (!(node
                        .getCondition()
                        .equals(org.openstack.atlas.service.domain.entity.NodeCondition.ENABLED) || node
                        .getCondition()
                        .equals(org.openstack.atlas.service.domain.entity.NodeCondition.DISABLED)))
                    Assert.fail("Did not map the NodeCondition of the node correctly");

                if (node.getStatus() == null)
                    continue;
                if (!node
                        .getStatus()
                        .equals(org.openstack.atlas.service.domain.entity.NodeStatus.OFFLINE))
                    Assert.fail("Did not map the NodeStatus of the node correctly");
            }
        }

        @Test
        public void should_map_the_virtual_ips_across_the_two_load_balancers_and_the_properties_of_individual_nodes() {
            Assert.assertEquals(1, domainLoadBalancer.getLoadBalancerJoinVipSet().size());

            for (LoadBalancerJoinVip loadBalancerJoinVip : domainLoadBalancer
                    .getLoadBalancerJoinVipSet()) {
                Assert.assertEquals(new Integer(2000), loadBalancerJoinVip.getVirtualIp().getId());
                Assert.assertEquals("10.10.10.1", loadBalancerJoinVip.getVirtualIp().getIpAddress());
                Assert.assertEquals(
                        org.openstack.atlas.service.domain.entity.VirtualIpType.PUBLIC,
                        loadBalancerJoinVip.getVirtualIp().getVipType());
            }
        }

        @Test
        public void should_map_the_connection_limits_across_the_two_load_balancers() {
            Assert.assertEquals(new Integer(101), domainLoadBalancer
                    .getConnectionThrottle().getMaxRequestRate());
            Assert.assertEquals(new Integer(103), domainLoadBalancer
                    .getConnectionThrottle().getRateInterval());
        }

        @Test
        public void should_map_health_monitor_and_its_properties() {
            org.openstack.atlas.service.domain.entity.HealthMonitor healthMonitor = domainLoadBalancer
                    .getHealthMonitor();
            Assert.assertEquals(null, healthMonitor.getId());
            Assert.assertEquals(new Integer(1),
                    healthMonitor.getAttemptsBeforeDeactivation());
            Assert.assertEquals(new Integer(1), healthMonitor.getDelay());
            Assert.assertEquals(new Integer(1), healthMonitor.getTimeout());
            Assert.assertEquals("some/path", healthMonitor.getPath());
            Assert.assertEquals(
                    org.openstack.atlas.service.domain.entity.HealthMonitorType.CONNECT,
                    healthMonitor.getType());
        }

    }
}
