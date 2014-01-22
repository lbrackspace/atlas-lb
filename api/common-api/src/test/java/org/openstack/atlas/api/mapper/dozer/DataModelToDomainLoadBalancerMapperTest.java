package org.openstack.atlas.api.mapper.dozer;

import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.docs.loadbalancers.api.v1.*;
import org.openstack.atlas.service.domain.entities.AccessListType;
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entities.VirtualIpType;

import java.util.GregorianCalendar;

@RunWith(Enclosed.class)
public class DataModelToDomainLoadBalancerMapperTest {
    private static final String publicDozerConfigFile = "loadbalancing-dozer-mapping.xml";
    public static class Wnen_mapping_a_load_balancer_from_datamodel_to_domain {
        private DozerBeanMapper mapper;
        private LoadBalancer loadBalancer;
        private org.openstack.atlas.service.domain.entities.LoadBalancer domainLoadBalancer;

        @Before
        public void setUp() {
            mapper = MapperBuilder.getConfiguredMapper(publicDozerConfigFile);

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
            loadBalancer.setHalfClosed(true);
            loadBalancer.setAlgorithm("ROUND_ROBIN");
            loadBalancer.setStatus("SUSPENDED");

            ConnectionLogging conLog = new ConnectionLogging();
            conLog.setEnabled(true);
            loadBalancer.setConnectionLogging(conLog);

            org.openstack.atlas.docs.loadbalancers.api.v1.Meta meta1 = new org.openstack.atlas.docs.loadbalancers.api.v1.Meta();
            meta1.setId(4100);
            meta1.setKey("metaKey1");
            meta1.setValue("metaValue1");

            org.openstack.atlas.docs.loadbalancers.api.v1.Meta meta2 = new org.openstack.atlas.docs.loadbalancers.api.v1.Meta();
            meta2.setId(4101);
            meta2.setKey("metaKey2");
            meta2.setValue("metaValue2");

            final Metadata metadata = new Metadata();
            metadata.getMetas().add(meta1);
            metadata.getMetas().add(meta2);
            loadBalancer.getMetadata().addAll(metadata.getMetas());

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
            throttle.setMaxConnectionRate(101);
            throttle.setMaxConnections(102);
            throttle.setMinConnections(100);
            throttle.setRateInterval(103);
            loadBalancer.setConnectionThrottle(throttle);

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

            domainLoadBalancer = mapper
                    .map(loadBalancer,
                            org.openstack.atlas.service.domain.entities.LoadBalancer.class);
        }

        @Test
        public void should_not_fail_when_data_model_loadBalancer_is_empty() {
            loadBalancer = new LoadBalancer();
            try {
                domainLoadBalancer = mapper
                        .map(loadBalancer,
                                org.openstack.atlas.service.domain.entities.LoadBalancer.class);
            } catch (Exception e) {
                Assert.fail("Empty domain load balancer caused this exception");
            }
        }

        @Test
        public void should_map_name_and_other_simple_types() {
//            Calendar cal = new GregorianCalendar(2010, 10, 10);
//            domainLoadBalancer.setCreated(cal);
//            domainLoadBalancer.setUpdated(cal);
            Assert.assertEquals(new Integer(100), domainLoadBalancer.getId());
            Assert.assertEquals("LB 1", domainLoadBalancer.getName());
            Assert.assertEquals(new Integer(9999), domainLoadBalancer.getPort());
            Assert.assertEquals(new GregorianCalendar(2010, 10, 10), domainLoadBalancer.getCreated());
            Assert.assertEquals(new GregorianCalendar(2010, 12, 10), domainLoadBalancer.getUpdated());
            Assert.assertEquals(true, domainLoadBalancer.isConnectionLogging());
        }

        @Test
        public void should_map_half_close() {
            Assert.assertEquals(true, domainLoadBalancer.isHalfClosed());
        }

        @Test
        public void should_map_half_close_false() {
            loadBalancer.setHalfClosed(false);
            domainLoadBalancer = mapper.map(loadBalancer, org.openstack.atlas.service.domain.entities.LoadBalancer.class);
            Assert.assertEquals(false, domainLoadBalancer.isHalfClosed());
        }

        @Test
        public void should_map_enumerations_on_the_loadbalancer() {
            Assert.assertEquals(org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.IMAPv4,
                    domainLoadBalancer.getProtocol());
            Assert.assertEquals(org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm.ROUND_ROBIN,
                    domainLoadBalancer.getAlgorithm());
            Assert.assertEquals(
                    org.openstack.atlas.service.domain.entities.LoadBalancerStatus.SUSPENDED,
                    domainLoadBalancer.getStatus());
        }

        @Test
        public void should_map_metadata_across_the_two_load_balancers_and_the_properties_of_individual_meta() {
            Assert.assertEquals(2, domainLoadBalancer.getLoadbalancerMetadata().size());

            for (org.openstack.atlas.service.domain.entities.LoadbalancerMeta loadbalancerMeta : domainLoadBalancer.getLoadbalancerMetadata()) {
                if (!(loadbalancerMeta.getId() == 4100 || loadbalancerMeta.getId() == 4101))
                    Assert.fail("Did not map the id of the loadbalancerMeta correctly");
                if (!(loadbalancerMeta.getKey().equals("metaKey1") || loadbalancerMeta.getKey().equals("metaKey2")))
                    Assert.fail("Did not map the key of the loadbalancerMeta correctly");
                if (!(loadbalancerMeta.getValue().equals("metaValue1") || loadbalancerMeta.getValue().equals("metaValue2")))
                    Assert.fail("Did not map the value of the loadbalancerMeta correctly");
            }
        }

        @Test
        public void should_map_the_node_list_across_the_two_load_balancers_and_the_properties_of_individual_nodes() {
            Assert.assertEquals(2, domainLoadBalancer.getNodes().size());

            for (org.openstack.atlas.service.domain.entities.Node node : domainLoadBalancer.getNodes()) {
                if (!(node.getId() == 4100 || node.getId() == 4101))
                    Assert.fail("Did not map the id of the node correctly");
                if (!(node.getPort() == 80 || node.getPort() == 85))
                    Assert.fail("Did not map the port of the node correctly");
                if (!(node.getIpAddress().equals("10.1.1.1") || node.getIpAddress().equals("10.1.1.2")))
                    Assert.fail("Did not map the ipAddress of the node correctly");
                if (!(node
                        .getCondition()
                        .equals(org.openstack.atlas.service.domain.entities.NodeCondition.DISABLED) || node
                        .getCondition()
                        .equals(org.openstack.atlas.service.domain.entities.NodeCondition.DRAINING)))
                    Assert.fail("Did not map the NodeCondition of the node correctly");

                if (node.getStatus() == null)
                    continue;
                if (!node
                        .getStatus()
                        .equals(org.openstack.atlas.service.domain.entities.NodeStatus.OFFLINE))
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
                        org.openstack.atlas.service.domain.entities.VirtualIpType.PUBLIC,
                        loadBalancerJoinVip.getVirtualIp().getVipType());
            }
        }

        @Test
        public void should_map_the_connection_limits_across_the_two_load_balancers() {
            Assert.assertEquals(new Integer(100), domainLoadBalancer
                    .getConnectionLimit().getMinConnections());
            Assert.assertEquals(new Integer(101), domainLoadBalancer
                    .getConnectionLimit().getMaxConnectionRate());
            Assert.assertEquals(new Integer(102), domainLoadBalancer
                    .getConnectionLimit().getMaxConnections());
            Assert.assertEquals(new Integer(103), domainLoadBalancer
                    .getConnectionLimit().getRateInterval());
        }

        @Test
        public void should_map_session_persistence_across_loadbalancers() {
            Assert.assertEquals(
                    org.openstack.atlas.service.domain.entities.SessionPersistence.HTTP_COOKIE,
                    domainLoadBalancer.getSessionPersistence());
        }

        @Test
        public void should_map_health_monitor_and_its_properties() {
            org.openstack.atlas.service.domain.entities.HealthMonitor healthMonitor = domainLoadBalancer
                    .getHealthMonitor();
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

    }

    public static class Wnen_mapping_a_load_balancer_from_datamodel_to_domain_with_ipv6_vips {
        private DozerBeanMapper mapper;
        private LoadBalancer loadBalancer;
        private org.openstack.atlas.service.domain.entities.LoadBalancer domainLoadBalancer;

        @Before
        public void setUp() {
            mapper = MapperBuilder.getConfiguredMapper(publicDozerConfigFile);

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
        }

        @Test
        public void should_map_the_virtual_across_the_two_load_balancers_with_type_only() {
            VirtualIp virtualIp1 = new VirtualIp();
            virtualIp1.setType(VipType.PUBLIC);
            loadBalancer.getVirtualIps().add(virtualIp1);
            domainLoadBalancer = mapper.map(loadBalancer, org.openstack.atlas.service.domain.entities.LoadBalancer.class);
            Assert.assertEquals(1, domainLoadBalancer.getLoadBalancerJoinVip6Set().size());
            Assert.assertEquals(1, domainLoadBalancer.getLoadBalancerJoinVipSet().size());
        }

        @Test
        public void should_map_the_virtual_ipv4_across_the_two_load_balancers_with_public_ipv4() {
            VirtualIp virtualIp1 = new VirtualIp();
            virtualIp1.setType(VipType.PUBLIC);
            virtualIp1.setIpVersion(IpVersion.IPV4);
            loadBalancer.getVirtualIps().add(virtualIp1);

            domainLoadBalancer = mapper.map(loadBalancer, org.openstack.atlas.service.domain.entities.LoadBalancer.class);
            Assert.assertEquals(1, domainLoadBalancer.getLoadBalancerJoinVipSet().size());

            for (LoadBalancerJoinVip jv : domainLoadBalancer.getLoadBalancerJoinVipSet()) {
                Assert.assertEquals(org.openstack.atlas.service.domain.entities.IpVersion.IPV4, jv.getVirtualIp().getIpVersion());
                Assert.assertEquals(VirtualIpType.PUBLIC, jv.getVirtualIp().getVipType());
            }
        }

        @Test
        public void should_map_the_virtual_across_the_two_load_balancers_with_ipv6_and_public() {
            VirtualIp virtualIp1 = new VirtualIp();
            virtualIp1.setType(VipType.PUBLIC);
            virtualIp1.setIpVersion(IpVersion.IPV6);
            loadBalancer.getVirtualIps().add(virtualIp1);
            domainLoadBalancer = mapper.map(loadBalancer, org.openstack.atlas.service.domain.entities.LoadBalancer.class);
            Assert.assertEquals(1, domainLoadBalancer.getLoadBalancerJoinVip6Set().size());

        }

        @Test
        public void should_map_the_virtual_across_the_two_load_balancers_with_ipv4_and_servicenet() {
            VirtualIp virtualIp1 = new VirtualIp();
            virtualIp1.setType(VipType.SERVICENET);
            virtualIp1.setIpVersion(IpVersion.IPV4);
            loadBalancer.getVirtualIps().add(virtualIp1);
            domainLoadBalancer = mapper.map(loadBalancer, org.openstack.atlas.service.domain.entities.LoadBalancer.class);
            Assert.assertEquals(1, domainLoadBalancer.getLoadBalancerJoinVipSet().size());

            for (LoadBalancerJoinVip jv : domainLoadBalancer.getLoadBalancerJoinVipSet()) {
                Assert.assertEquals(org.openstack.atlas.service.domain.entities.IpVersion.IPV4, jv.getVirtualIp().getIpVersion());
                Assert.assertEquals(VirtualIpType.SERVICENET, jv.getVirtualIp().getVipType());
            }
        }

        @Test
        public void should_map_the_virtual_across_the_two_load_balancers_with_servicenet_only() {
            VirtualIp virtualIp1 = new VirtualIp();
            virtualIp1.setType(VipType.SERVICENET);
            loadBalancer.getVirtualIps().add(virtualIp1);
            domainLoadBalancer = mapper.map(loadBalancer, org.openstack.atlas.service.domain.entities.LoadBalancer.class);
            Assert.assertEquals(1, domainLoadBalancer.getLoadBalancerJoinVipSet().size());

            for (LoadBalancerJoinVip jv : domainLoadBalancer.getLoadBalancerJoinVipSet()) {
                Assert.assertEquals(org.openstack.atlas.service.domain.entities.IpVersion.IPV4, jv.getVirtualIp().getIpVersion());
                Assert.assertEquals(VirtualIpType.SERVICENET, jv.getVirtualIp().getVipType());
            }
        }

        @Test
        public void should_map_the_virtual_ipv6_across_the_two_load_balancers_with_ipv6_id() {
            VirtualIp virtualIp1 = new VirtualIp();
            virtualIp1.setId(9000000);
            loadBalancer.getVirtualIps().add(virtualIp1);
            domainLoadBalancer = mapper.map(loadBalancer, org.openstack.atlas.service.domain.entities.LoadBalancer.class);
            Assert.assertEquals(1, domainLoadBalancer.getLoadBalancerJoinVip6Set().size());

            for (LoadBalancerJoinVip jv : domainLoadBalancer.getLoadBalancerJoinVipSet()) {
                Assert.assertEquals(null, jv.getVirtualIp().getIpVersion());
                Assert.assertEquals(null, jv.getVirtualIp().getVipType());
            }
        }

        @Test
        public void should_map_the_virtual_ipv4_across_the_two_load_balancers_with_ipv4_id() {
            VirtualIp virtualIp1 = new VirtualIp();
            virtualIp1.setId(12);
            loadBalancer.getVirtualIps().add(virtualIp1);
            domainLoadBalancer = mapper.map(loadBalancer, org.openstack.atlas.service.domain.entities.LoadBalancer.class);
            Assert.assertEquals(1, domainLoadBalancer.getLoadBalancerJoinVipSet().size());

            for (LoadBalancerJoinVip jv : domainLoadBalancer.getLoadBalancerJoinVipSet()) {
                Assert.assertEquals(null, jv.getVirtualIp().getIpVersion());
                Assert.assertEquals(null, jv.getVirtualIp().getVipType());
            }
        }
    }

    public static class Wnen_mapping_an_access_list_from_datamodel_to_domain {

        private DozerBeanMapper mapper;
        private AccessList accessList;
        private org.openstack.atlas.service.domain.entities.LoadBalancer domainLoadBalancer;
        @Before
        public void standUp() {
            mapper = MapperBuilder.getConfiguredMapper(publicDozerConfigFile);

            accessList = new AccessList();
            NetworkItem networkItem1 = new NetworkItem();
            NetworkItem networkItem2 = new NetworkItem();

            networkItem1.setAddress("ip1");
            networkItem2.setAddress("ip2");
            networkItem1.setIpVersion(IpVersion.IPV4);
            networkItem2.setIpVersion(IpVersion.IPV4);
            networkItem1.setType(NetworkItemType.ALLOW);
            networkItem2.setType(NetworkItemType.DENY);

            accessList.getNetworkItems().add(networkItem1);
            accessList.getNetworkItems().add(networkItem2);

            domainLoadBalancer = mapper
                    .map(accessList,
                            org.openstack.atlas.service.domain.entities.LoadBalancer.class);
        }

        @Test
        public void should_not_fail_when_data_model_access_list_is_empty() {
            accessList = new AccessList();
            try {
                domainLoadBalancer = mapper
                        .map(accessList,
                                org.openstack.atlas.service.domain.entities.LoadBalancer.class);
            } catch (Exception e) {
                Assert.fail("Empty domain load balancer caused this exception");
            }
        }

        @Test
        public void should_map_network_items_to_access_list_items() {
            for (org.openstack.atlas.service.domain.entities.AccessList list : domainLoadBalancer
                    .getAccessLists()) {
                if (!(list.getIpAddress().equals("ip1") || list.getIpAddress()
                        .equals("ip2")))
                    Assert.fail("Did not map the ip address of the network item correctly");
                if (!(list.getType().equals(AccessListType.ALLOW) || list
                        .getType().equals(AccessListType.DENY)))
                    Assert.fail("Did not map the access type of the network item correctly");
            }
        }

    }
}
