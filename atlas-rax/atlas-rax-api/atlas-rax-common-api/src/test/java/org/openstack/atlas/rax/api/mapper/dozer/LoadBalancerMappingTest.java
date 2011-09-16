package org.openstack.atlas.rax.api.mapper.dozer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.core.api.v1.ConnectionThrottle;
import org.openstack.atlas.core.api.v1.LoadBalancer;
import org.openstack.atlas.core.api.v1.SessionPersistence;
import org.openstack.atlas.core.api.v1.VipType;
import org.openstack.atlas.datamodel.CoreNodeCondition;
import org.openstack.atlas.rax.api.mapper.dozer.converter.AnyObjectMapper;
import org.openstack.atlas.rax.domain.entity.AccessList;
import org.openstack.atlas.rax.domain.stub.RaxStubFactory;
import org.openstack.atlas.service.domain.entity.*;

import javax.xml.namespace.QName;
import java.util.List;

@RunWith(Enclosed.class)
public class LoadBalancerMappingTest {

    public static class WhenMappingALoadBalancerFromDataModelToRaxDomain extends MappingBase {
        private LoadBalancer dataModelLoadBalancer;
        private org.openstack.atlas.rax.domain.entity.RaxLoadBalancer domainLoadBalancer;

        @Before
        public void setUp() {
            dataModelLoadBalancer = RaxStubFactory.createHydratedDataModelLoadBalancer();
            domainLoadBalancer = mapper.map(dataModelLoadBalancer, org.openstack.atlas.rax.domain.entity.RaxLoadBalancer.class);
        }

        @Test
        public void should_not_fail_when_data_model_loadBalancer_is_empty() {
            dataModelLoadBalancer = new LoadBalancer();
            try {
                domainLoadBalancer = mapper.map(dataModelLoadBalancer, org.openstack.atlas.rax.domain.entity.RaxLoadBalancer.class);
            } catch (Exception e) {
                Assert.fail("Empty domain load balancer caused this exception");
            }
        }

        @Test
        public void should_map_name_and_other_simple_types() {
            Assert.assertEquals(dataModelLoadBalancer.getId(), domainLoadBalancer.getId());
            Assert.assertEquals(dataModelLoadBalancer.getName(), domainLoadBalancer.getName());
            Assert.assertEquals(dataModelLoadBalancer.getPort(), domainLoadBalancer.getPort());
            Assert.assertEquals(dataModelLoadBalancer.getCreated().getTime(), domainLoadBalancer.getCreated());
            Assert.assertEquals(dataModelLoadBalancer.getUpdated().getTime(), domainLoadBalancer.getUpdated());
        }

        @Test
        public void should_map_enumerations_on_the_loadbalancer() {
            Assert.assertEquals(dataModelLoadBalancer.getProtocol(), domainLoadBalancer.getProtocol().name());
            Assert.assertEquals(dataModelLoadBalancer.getAlgorithm(), domainLoadBalancer.getAlgorithm().name());
            Assert.assertEquals(dataModelLoadBalancer.getStatus(), domainLoadBalancer.getStatus().name());
        }

        @Test
        public void should_map_the_node_list_across_the_two_load_balancers_and_the_properties_of_individual_nodes() {
            Assert.assertEquals(dataModelLoadBalancer.getNodes().size(), domainLoadBalancer.getNodes().size());

            for (Node node : domainLoadBalancer.getNodes()) {
                if (!(node.getId() == 1 || node.getId() == 2))
                    Assert.fail("Did not map the id of the node correctly");
                if (!(node.getPort() == 80 || node.getPort() == 81))
                    Assert.fail("Did not map the port of the node correctly");
                if (!(node.getAddress().equals("10.1.1.1") || node.getAddress().equals("10.1.1.2")))
                    Assert.fail("Did not map the ipAddress of the node correctly");
                if (!(node.getCondition().equals(NodeCondition.ENABLED) ||
                        node.getCondition().equals(NodeCondition.DISABLED)))
                    Assert.fail("Did not map the NodeCondition of the node correctly");

                if (node.getStatus() == null)
                    continue;
                if (!(node.getStatus().equals(NodeStatus.ONLINE) ||
                    node.getStatus().equals(NodeStatus.OFFLINE)))
                    Assert.fail("Did not map the NodeStatus of the node correctly");
            }
        }

        @Test
        public void should_map_the_virtual_ips_across_the_two_load_balancers_and_the_properties_of_individual_nodes() {
            Assert.assertEquals(dataModelLoadBalancer.getVirtualIps().size(), domainLoadBalancer.getLoadBalancerJoinVipSet().size());

            for (LoadBalancerJoinVip loadBalancerJoinVip : domainLoadBalancer.getLoadBalancerJoinVipSet()) {
                Assert.assertEquals(new Integer(1), loadBalancerJoinVip.getVirtualIp().getId());
                Assert.assertEquals("10.10.10.1", loadBalancerJoinVip.getVirtualIp().getAddress());
                Assert.assertEquals(VirtualIpType.PUBLIC, loadBalancerJoinVip.getVirtualIp().getVipType());
            }
        }

        @Test
        public void should_map_the_connection_limits_across_the_two_load_balancers() {
            Assert.assertEquals(dataModelLoadBalancer.getConnectionThrottle().getMaxRequestRate(), domainLoadBalancer.getConnectionThrottle().getMaxRequestRate());
            Assert.assertEquals(dataModelLoadBalancer.getConnectionThrottle().getRateInterval(), domainLoadBalancer.getConnectionThrottle().getRateInterval());
        }

        @Test
        public void should_map_health_monitor_and_its_properties() {
            HealthMonitor healthMonitor = domainLoadBalancer.getHealthMonitor();
            Assert.assertEquals(null, healthMonitor.getId());
            Assert.assertEquals(dataModelLoadBalancer.getHealthMonitor().getAttemptsBeforeDeactivation(), healthMonitor.getAttemptsBeforeDeactivation());
            Assert.assertEquals(dataModelLoadBalancer.getHealthMonitor().getDelay(), healthMonitor.getDelay());
            Assert.assertEquals(dataModelLoadBalancer.getHealthMonitor().getTimeout(), healthMonitor.getTimeout());
            Assert.assertEquals(dataModelLoadBalancer.getHealthMonitor().getPath(), healthMonitor.getPath());
            Assert.assertEquals(dataModelLoadBalancer.getHealthMonitor().getType(), healthMonitor.getType().name());
        }

        @Test
        public void shouldMapCrazyNameAttributeFromOtherAttributes() {
            String crazyNameValue = dataModelLoadBalancer.getOtherAttributes().get(new QName("http://docs.openstack.org/atlas/api/v1.1/extensions/rax", "crazyName", "rax"));
            Assert.assertEquals(crazyNameValue, domainLoadBalancer.getCrazyName());
        }
    }

    public static class WhenMappingALoadBalancerFromRaxDomainToDataModel extends MappingBase {

        private org.openstack.atlas.rax.domain.entity.RaxLoadBalancer domainLoadBalancer;
        private LoadBalancer dataModelLoadBalancer;

        @Before
        public void setUp() {
            domainLoadBalancer = RaxStubFactory.createHydratedDomainLoadBalancer();
            dataModelLoadBalancer = mapper.map(domainLoadBalancer, LoadBalancer.class);
        }

        @Test
        public void should_not_fail_when_domain_loadBalancer_is_empty() {
            domainLoadBalancer = new org.openstack.atlas.rax.domain.entity.RaxLoadBalancer();
            try {
                dataModelLoadBalancer = mapper.map(domainLoadBalancer, LoadBalancer.class);
            } catch (Exception e) {
                Assert.fail("Empty domain load balancer caused this exception");
            }
        }

        @Test
        public void should_map_name_and_other_simple_types() {
            Assert.assertEquals(domainLoadBalancer.getId(), dataModelLoadBalancer.getId());
            Assert.assertEquals(domainLoadBalancer.getName(), dataModelLoadBalancer.getName());
            Assert.assertEquals(domainLoadBalancer.getPort(), dataModelLoadBalancer.getPort());
            Assert.assertEquals(domainLoadBalancer.getCreated(), dataModelLoadBalancer.getCreated().getTime());
            Assert.assertEquals(domainLoadBalancer.getUpdated(), dataModelLoadBalancer.getUpdated().getTime());
            Assert.assertEquals(domainLoadBalancer.getConnectionLogging(), dataModelLoadBalancer.getConnectionLogging().isEnabled());
        }

        @Test
        public void should_map_all_loadbalancer_enumerations() {
            Assert.assertTrue(dataModelLoadBalancer.getStatus().equals(domainLoadBalancer.getStatus().name()));
            Assert.assertTrue(dataModelLoadBalancer.getProtocol().equals(domainLoadBalancer.getProtocol().name()));
            Assert.assertTrue(dataModelLoadBalancer.getAlgorithm().equals(domainLoadBalancer.getAlgorithm().name()));
        }

        @Test
        public void should_map_nodes_and_its_properties() {
            final List<org.openstack.atlas.core.api.v1.Node> list = dataModelLoadBalancer.getNodes();
            Assert.assertEquals(domainLoadBalancer.getNodes().size(), list.size());

            for (org.openstack.atlas.core.api.v1.Node node : list) {
                if (!(node.getId() == 1 || node.getId() == 2)) {
                    Assert.fail();
                }
                if (!(node.getPort() == 80 || node.getPort() == 81)) {
                    Assert.fail();
                }
                if (!(node.getAddress().equals("10.1.1.1") || node.getAddress().equals("10.1.1.2"))) {
                    Assert.fail();
                }
                if (!(node.getCondition().equals(CoreNodeCondition.DISABLED)
                        || node.getCondition().equals(CoreNodeCondition.ENABLED))) {
                    Assert.fail();
                }
                if (!(node.getStatus().equals(NodeStatus.OFFLINE.name())
                        || node.getStatus().equals(NodeStatus.ONLINE.name()))) {
                    Assert.fail();
                }
            }
        }

        @Test
        public void should_map_virtual_ips_across_two_loadbalancers() {
            final List<org.openstack.atlas.core.api.v1.VirtualIp> list = dataModelLoadBalancer.getVirtualIps();
            Assert.assertEquals(domainLoadBalancer.getLoadBalancerJoinVipSet().size(), list.size());

            for (org.openstack.atlas.core.api.v1.VirtualIp virtualIp : list) {
                if (!(virtualIp.getId() == 1 || virtualIp.getId() == 2)) {
                    Assert.fail();
                }
                if (!(virtualIp.getType().equals(VipType.PRIVATE) || virtualIp.getType().equals(VipType.PUBLIC))) {
                    Assert.fail();
                }
            }
        }

        @Test
        public void should_map_connection_limits_across_the_two_loadbalancers() {
            ConnectionThrottle throttle = dataModelLoadBalancer.getConnectionThrottle();
            Assert.assertEquals(domainLoadBalancer.getConnectionThrottle().getMaxRequestRate(), throttle.getMaxRequestRate());
            Assert.assertEquals(domainLoadBalancer.getConnectionThrottle().getRateInterval(), throttle.getRateInterval());
        }

        @Test
        public void should_map_session_persistence() {
            final SessionPersistence sessionPersistence = dataModelLoadBalancer.getSessionPersistence();
            Assert.assertEquals(domainLoadBalancer.getSessionPersistence().name(), sessionPersistence.getPersistenceType());
        }

        @Test
        public void should_map_session_persistence_to_null_when_data_model_session_persistence_is_set_to_none() {
            domainLoadBalancer.setSessionPersistence(org.openstack.atlas.service.domain.entity.SessionPersistence.NONE);
            dataModelLoadBalancer = mapper.map(domainLoadBalancer, LoadBalancer.class);
            Assert.assertNull(dataModelLoadBalancer.getSessionPersistence());
        }

        @Test
        public void should_map_health_monitor_and_its_properties() {
            org.openstack.atlas.core.api.v1.HealthMonitor healthMonitor = dataModelLoadBalancer.getHealthMonitor();
            Assert.assertEquals(domainLoadBalancer.getHealthMonitor().getAttemptsBeforeDeactivation(), healthMonitor.getAttemptsBeforeDeactivation());
            Assert.assertEquals((domainLoadBalancer.getHealthMonitor().getDelay()), healthMonitor.getDelay());
            Assert.assertEquals((domainLoadBalancer.getHealthMonitor().getTimeout()), healthMonitor.getTimeout());
            Assert.assertEquals((domainLoadBalancer.getHealthMonitor().getPath()), healthMonitor.getPath());
            Assert.assertEquals((domainLoadBalancer.getHealthMonitor().getType().name()), healthMonitor.getType());
        }

        @Test
        public void shouldMapCrazyNameToOtherAttributes() {
            String crazyNameValue = dataModelLoadBalancer.getOtherAttributes().get(new QName("http://docs.openstack.org/atlas/api/v1.1/extensions/rax", "crazyName", "rax"));
            Assert.assertEquals(domainLoadBalancer.getCrazyName(), crazyNameValue);
        }

        @Test
        public void shouldMapAccessListWhenAccessListExists() {
            org.openstack.atlas.api.v1.extensions.rax.AccessList accessList = AnyObjectMapper.getAnyElement(dataModelLoadBalancer.getAnies(), org.openstack.atlas.api.v1.extensions.rax.AccessList.class);
            Assert.assertNotNull(accessList);
        }
    }
}
