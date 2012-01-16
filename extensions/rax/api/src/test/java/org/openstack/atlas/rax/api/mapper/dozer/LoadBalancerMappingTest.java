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
import org.openstack.atlas.datamodel.CoreNodeStatus;
import org.openstack.atlas.rax.api.mapper.dozer.converter.ExtensionObjectMapper;
import org.openstack.atlas.rax.domain.entity.RaxAccessList;
import org.openstack.atlas.rax.domain.stub.RaxStubFactory;
import org.openstack.atlas.service.domain.entity.HealthMonitor;
import org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entity.Node;
import org.openstack.atlas.service.domain.entity.VirtualIpType;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Set;

@RunWith(Enclosed.class)
public class LoadBalancerMappingTest {

    public static class WhenMappingALoadBalancerFromDataModelToRaxDomain extends MappingBase {
        private LoadBalancer dataModelLoadBalancer;
        private org.openstack.atlas.rax.domain.entity.RaxLoadBalancer domainLoadBalancer;

        @Before
        public void setUp() throws Exception {
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
            Assert.assertEquals(dataModelLoadBalancer.getCreated(), domainLoadBalancer.getCreated());
            Assert.assertEquals(dataModelLoadBalancer.getUpdated(), domainLoadBalancer.getUpdated());
        }

        @Test
        public void should_map_enumerations_on_the_loadbalancer() {
            Assert.assertEquals(dataModelLoadBalancer.getProtocol(), domainLoadBalancer.getProtocol());
            Assert.assertEquals(dataModelLoadBalancer.getAlgorithm(), domainLoadBalancer.getAlgorithm());
            Assert.assertEquals(dataModelLoadBalancer.getStatus(), domainLoadBalancer.getStatus());
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
                if (node.isEnabled() == null)
                    Assert.fail("Did not map the NodeCondition of the node correctly");
                if (node.getStatus() == null)
                    continue;
                if (!(node.getStatus().equals(CoreNodeStatus.ONLINE) ||
                        node.getStatus().equals(CoreNodeStatus.OFFLINE)))
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
            Assert.assertEquals(dataModelLoadBalancer.getHealthMonitor().getType(), healthMonitor.getType());
        }

        @Test
        public void shouldMapCrazyNameAttributeFromOtherAttributes() {
            String crazyNameValue = ExtensionObjectMapper.getOtherAttribute(dataModelLoadBalancer.getOtherAttributes(), "crazyName");
            Assert.assertEquals(crazyNameValue, domainLoadBalancer.getCrazyName());
        }

        @Test
        public void shouldMapAccessListFromAniesList() {
            Set<RaxAccessList> accessListSet = domainLoadBalancer.getAccessLists();
            org.openstack.atlas.api.v1.extensions.rax.AccessList apiAccessList = ExtensionObjectMapper.getAnyElement(dataModelLoadBalancer.getAnies(), org.openstack.atlas.api.v1.extensions.rax.AccessList.class);

            Assert.assertFalse(accessListSet.isEmpty());

            for (RaxAccessList accessList : accessListSet) {
                Assert.assertEquals(apiAccessList.getNetworkItems().get(0).getAddress(), accessList.getIpAddress());
                //Assert.assertEquals(apiAccessList.getNetworkItems().get(0).getIpVersion().name(), accessList.getIpVersion().name());
                Assert.assertEquals(apiAccessList.getNetworkItems().get(0).getType().name(), accessList.getType().name());
            }
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
            Assert.assertEquals(domainLoadBalancer.getCreated(), dataModelLoadBalancer.getCreated());
            Assert.assertEquals(domainLoadBalancer.getUpdated(), dataModelLoadBalancer.getUpdated());
        }

        @Test
        public void should_map_all_loadbalancer_enumerations() {
            Assert.assertTrue(dataModelLoadBalancer.getStatus().equals(domainLoadBalancer.getStatus()));
            Assert.assertTrue(dataModelLoadBalancer.getProtocol().equals(domainLoadBalancer.getProtocol()));
            Assert.assertTrue(dataModelLoadBalancer.getAlgorithm().equals(domainLoadBalancer.getAlgorithm()));
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
                if (node.isEnabled() == null) {
                    Assert.fail();
                }
                if (!(node.getStatus().equals(CoreNodeStatus.OFFLINE)
                        || node.getStatus().equals(CoreNodeStatus.ONLINE))) {
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
            Assert.assertEquals(domainLoadBalancer.getSessionPersistence().getPersistenceType(), sessionPersistence.getPersistenceType());
        }

        @Test
        public void should_map_session_persistence_to_null_when_data_model_session_persistence_is_set_to_null() {
            domainLoadBalancer.setSessionPersistence(null);
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
            Assert.assertEquals((domainLoadBalancer.getHealthMonitor().getType()), healthMonitor.getType());
        }

        @Test
        public void shouldMapCrazyNameToOtherAttributes() {
            String crazyNameValue = dataModelLoadBalancer.getOtherAttributes().get(new QName("http://docs.openstack.org/atlas/api/v1.1/extensions/rax", "crazyName", "rax"));
            Assert.assertEquals(domainLoadBalancer.getCrazyName(), crazyNameValue);
        }

        @Test
        public void shouldMapAccessListWhenAccessListExists() {
            org.openstack.atlas.api.v1.extensions.rax.AccessList apiAccessList = ExtensionObjectMapper.getAnyElement(dataModelLoadBalancer.getAnies(), org.openstack.atlas.api.v1.extensions.rax.AccessList.class);
            RaxAccessList accessList = domainLoadBalancer.getAccessLists().iterator().next();

            Assert.assertNotNull(apiAccessList);
            Assert.assertEquals(accessList.getIpAddress(), apiAccessList.getNetworkItems().get(0).getAddress());
            //Assert.assertEquals(accessList.getIpVersion().name(), apiAccessList.getNetworkItems().get(0).getIpVersion().name());
            Assert.assertEquals(accessList.getType().name(), apiAccessList.getNetworkItems().get(0).getType().name());
        }
    }
}
