package org.openstack.atlas.api.mapper.dozer;

import org.openstack.atlas.docs.loadbalancers.api.v1.AccessList;
import org.openstack.atlas.docs.loadbalancers.api.v1.*;
import org.openstack.atlas.docs.loadbalancers.api.v1.SessionPersistence;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.HealthMonitor;
import org.openstack.atlas.service.domain.entities.HealthMonitorType;
import org.openstack.atlas.service.domain.entities.IpVersion;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.LoadbalancerMeta;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeCondition;
import org.openstack.atlas.service.domain.entities.NodeStatus;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.*;

@RunWith(Enclosed.class)
public class DomainToDataModelLoadBalancerMapperTest {

    private static final String publicDozerConfigFile = "loadbalancing-dozer-mapping.xml";
    private static final String managementDozerConfigFile = "loadbalancing-dozer-management-mapping.xml";

    public static class When_mapping_a_domain_protocolobject_to_a_datamodel_protocol {

        private DozerBeanMapper mapper;
        private org.openstack.atlas.docs.loadbalancers.api.v1.Protocol expected_prot;
        private org.openstack.atlas.docs.loadbalancers.api.v1.Protocol actual_prot;
        private org.openstack.atlas.service.domain.entities.LoadBalancerProtocolObject dprot;

        @Before
        public void setUp() {
            mapper = MapperBuilder.getConfiguredMapper(publicDozerConfigFile);

            expected_prot = new org.openstack.atlas.docs.loadbalancers.api.v1.Protocol();
            expected_prot.setName("IMAPv4");
            expected_prot.setPort(80);

            dprot = new org.openstack.atlas.service.domain.entities.LoadBalancerProtocolObject();
            dprot.setPort(80);
            dprot.setName(LoadBalancerProtocol.IMAPv4);

            actual_prot = new org.openstack.atlas.docs.loadbalancers.api.v1.Protocol();
        }

        private static String classMissMatch(Class expected, Class actual) {
            String out;
            String format = "Expected mapper to return %s but got a %s object instead";
            out = String.format(format, expected.getCanonicalName(), actual.getCanonicalName());
            return out;
        }

        @Test
        public void should_map_from_loadbalancerprotocolobject_to_data_model_procolot_with_out_any_problems() {
            String msg;
            Object obj = mapper.map(dprot, org.openstack.atlas.docs.loadbalancers.api.v1.Protocol.class);

            msg = classMissMatch(expected_prot.getClass(), actual_prot.getClass());
            Assert.assertTrue(msg, obj instanceof org.openstack.atlas.docs.loadbalancers.api.v1.Protocol);
            actual_prot = (org.openstack.atlas.docs.loadbalancers.api.v1.Protocol) obj;
            Assert.assertEquals(expected_prot.getPort(), actual_prot.getPort());
            Assert.assertEquals(expected_prot.getName(), actual_prot.getName());

            msg = classMissMatch(java.lang.String.class, actual_prot.getName().getClass());
            Assert.assertTrue(msg, actual_prot.getName() instanceof java.lang.String);
        }
    }

    public static class When_mapping_a_domain_algo_to_a_datamodel_algo {

        private DozerBeanMapper mapper;
        private org.openstack.atlas.docs.loadbalancers.api.v1.Algorithm expected_algo;
        private org.openstack.atlas.docs.loadbalancers.api.v1.Algorithm actual_algo;
        private org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithmObject dalgo;

        @Before
        public void setUp() {
            mapper = MapperBuilder.getConfiguredMapper(publicDozerConfigFile);

            expected_algo = new org.openstack.atlas.docs.loadbalancers.api.v1.Algorithm();
            expected_algo.setName("WEIGHTED_ROUND_ROBIN");

            dalgo = new org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithmObject();
            dalgo.setName(LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN);

            actual_algo = new org.openstack.atlas.docs.loadbalancers.api.v1.Algorithm();
        }

        private static String classMissMatch(Class expected, Class actual) {
            String out;
            String format = "Expected mapper to return %s but got a %s object instead";
            out = String.format(format, expected.getCanonicalName(), actual.getCanonicalName());
            return out;
        }

        @Test
        public void should_map_all_enums_between_domain_algo_and_datamodel_algo() {
            for (org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm dataAlgoType : org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm.values()) {
                String enumString = dataAlgoType.name();
                dalgo.setName(dataAlgoType);
                expected_algo.setName(enumString);
                String msg;
                Object obj = mapper.map(dalgo, org.openstack.atlas.docs.loadbalancers.api.v1.Algorithm.class);

                msg = classMissMatch(expected_algo.getClass(), actual_algo.getClass());
                Assert.assertTrue(msg, obj instanceof org.openstack.atlas.docs.loadbalancers.api.v1.Algorithm);
                actual_algo = (org.openstack.atlas.docs.loadbalancers.api.v1.Algorithm) obj;
                Assert.assertEquals(expected_algo.getName(), actual_algo.getName());

                msg = classMissMatch(org.openstack.atlas.docs.loadbalancers.api.v1.AlgorithmType.class, actual_algo.getName().getClass());
                Assert.assertTrue(msg, actual_algo.getName() instanceof java.lang.String);
            }
        }
    }

    public static class When_mapping_a_load_balancer_from_domain_to_datamodel {

        private DozerBeanMapper mapper;
        private LoadBalancer loadBalancer;
        private org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer dataModelLoadBalancer;

        @Before
        public void setUp() {
            mapper = MapperBuilder.getConfiguredMapper(publicDozerConfigFile);
            loadBalancer = createHydratedLoadbalancer();
            dataModelLoadBalancer = mapper.map(loadBalancer, org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer.class);
        }

        @Test
        public void should_not_fail_when_domain_loadBalancer_is_empty() {
            loadBalancer = new LoadBalancer();
            try {
                dataModelLoadBalancer = mapper.map(loadBalancer, org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer.class);
            } catch (Exception e) {
                Assert.fail("Empty domain load balancer caused this exception");
            }
        }

        @Test
        public void should_map_name_and_other_simple_types() {
            Assert.assertEquals(new Integer(100), dataModelLoadBalancer.getId());
            Assert.assertEquals("LB 1", dataModelLoadBalancer.getName());
            Assert.assertEquals(new Integer(9999), dataModelLoadBalancer.getPort());
            Assert.assertEquals(new GregorianCalendar(2010, 10, 10), dataModelLoadBalancer.getCreated().getTime());
            Assert.assertEquals(new GregorianCalendar(2010, 10, 10), dataModelLoadBalancer.getUpdated().getTime());
            Assert.assertTrue(dataModelLoadBalancer.getConnectionLogging().isEnabled());
        }

        @Test
        public void should_map_half_close() {
            Assert.assertEquals(true, dataModelLoadBalancer.isHalfClosed());
        }

        @Test
        public void should_map_half_close_when_false() {
            loadBalancer.setHalfClosed(false);
            dataModelLoadBalancer = mapper.map(loadBalancer, org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer.class);
            Assert.assertEquals(false, dataModelLoadBalancer.isHalfClosed());
        }

        @Test
        public void should_map_all_loadbalancer_enumerations() {
            Assert.assertTrue(dataModelLoadBalancer.getStatus().equals("PENDING_UPDATE"));
            Assert.assertTrue(dataModelLoadBalancer.getProtocol().equals("IMAPv4"));
            Assert.assertTrue(dataModelLoadBalancer.getAlgorithm().equals("WEIGHTED_ROUND_ROBIN"));
        }

        @Test
        public void should_map_metadata_to_null_if_null() {
            loadBalancer.setLoadbalancerMetadata(null);
            dataModelLoadBalancer = mapper.map(loadBalancer, org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer.class);
            Assert.assertNull(dataModelLoadBalancer.getMetadata());
        }

        @Test
        public void should_map_metadata_and_its_properties() {
            final List<org.openstack.atlas.docs.loadbalancers.api.v1.Meta> list = dataModelLoadBalancer.getMetadata();
            Assert.assertEquals(2, list.size());

            for (org.openstack.atlas.docs.loadbalancers.api.v1.Meta meta : list) {
                if (!(meta.getId() == 991 || meta.getId() == 992)) {
                    Assert.fail();
                }
                if (!(meta.getKey().equals("metaKey1") || meta.getKey().equals("metaKey2"))) {
                    Assert.fail();
                }
                if (!(meta.getValue().equals("metaValue1") || meta.getValue().equals("metaValue2"))) {
                    Assert.fail();
                }
            }
        }

        @Test
        public void should_map_nodes_and_its_properties() {
            final List<org.openstack.atlas.docs.loadbalancers.api.v1.Node> list = dataModelLoadBalancer.getNodes();
            Assert.assertEquals(2, list.size());

            for (org.openstack.atlas.docs.loadbalancers.api.v1.Node node : list) {
                if (!(node.getId() == 100 || node.getId() == 101)) {
                    Assert.fail();
                }
                if (!(node.getPort() == 1000 || node.getPort() == 1001)) {
                    Assert.fail();
                }
                if (!(node.getAddress().equals("ip1") || node.getAddress().equals("ip2"))) {
                    Assert.fail();
                }
                if (!(node.getCondition().equals(org.openstack.atlas.docs.loadbalancers.api.v1.NodeCondition.DISABLED)
                        || node.getCondition().equals(org.openstack.atlas.docs.loadbalancers.api.v1.NodeCondition.ENABLED))) {
                    Assert.fail();
                }
                if (!(node.getStatus().equals(org.openstack.atlas.docs.loadbalancers.api.v1.NodeStatus.OFFLINE)
                        || node.getStatus().equals(org.openstack.atlas.docs.loadbalancers.api.v1.NodeStatus.ONLINE))) {
                    Assert.fail();
                }
            }
        }

        @Test
        public void should_map_virtual_ips_across_two_loadbalancers() {
            final List<org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp> list = dataModelLoadBalancer.getVirtualIps();
            Assert.assertEquals(3, list.size());

            for (org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp virtualIp : list) {
                if (!(virtualIp.getId() == 100 || virtualIp.getId() == 101 || virtualIp.getId() == 1)) {
                    Assert.fail();
                }
                if (!(virtualIp.getType().equals(VipType.SERVICENET) || virtualIp.getType().equals(VipType.PUBLIC))) {
                    Assert.fail();
                }
            }
        }

        @Test
        public void should_map_connection_limits_across_the_two_loadbalancers() {
            ConnectionThrottle throttle = dataModelLoadBalancer.getConnectionThrottle();

            Assert.assertEquals((Object) 10, throttle.getMaxConnectionRate());
            Assert.assertEquals((Object) 11, throttle.getMaxConnections());
            Assert.assertEquals((Object) 12, throttle.getMinConnections());
            Assert.assertEquals((Object) 13, throttle.getRateInterval());
        }

        @Test
        public void should_map_session_persistence() {
            final SessionPersistence sessionPersistence = dataModelLoadBalancer.getSessionPersistence();

            Assert.assertEquals(PersistenceType.HTTP_COOKIE, sessionPersistence.getPersistenceType());
        }

        @Test
        public void should_map_session_persistence_to_null_when_data_model_session_persistence_is_set_to_none() {
            loadBalancer.setSessionPersistence(org.openstack.atlas.service.domain.entities.SessionPersistence.NONE);
            dataModelLoadBalancer = mapper.map(loadBalancer, org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer.class);

            Assert.assertNull(dataModelLoadBalancer.getSessionPersistence());
        }

        @Test
        public void should_map_health_monitor_and_its_properties() {
            org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitor healthMonitor = dataModelLoadBalancer.getHealthMonitor();
            Assert.assertEquals(null, healthMonitor.getId());
            Assert.assertEquals(new Integer(1), healthMonitor.getAttemptsBeforeDeactivation());
            Assert.assertEquals(new Integer(1), healthMonitor.getDelay());
            Assert.assertEquals(new Integer(1), healthMonitor.getTimeout());
            Assert.assertEquals("some regex", healthMonitor.getBodyRegex());
            Assert.assertEquals("some regex", healthMonitor.getStatusRegex());
            Assert.assertEquals("some/path", healthMonitor.getPath());
            Assert.assertEquals(org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitorType.CONNECT, healthMonitor.getType());
        }
    }

    public static class When_mapping_a_load_balancer_with_empty_collections_from_domain_to_datamodel {

        private DozerBeanMapper mapper;
        private LoadBalancer loadBalancer;
        private org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer dataModelLoadBalancer;

        @Before
        public void setUp() {
            mapper = MapperBuilder.getConfiguredMapper(publicDozerConfigFile);

            loadBalancer = new LoadBalancer();
            loadBalancer.setAccessLists(new HashSet<org.openstack.atlas.service.domain.entities.AccessList>());
            loadBalancer.setNodes(new HashSet<Node>());
            loadBalancer.setLoadBalancerJoinVipSet(new HashSet<LoadBalancerJoinVip>());
            loadBalancer.setUsage(new HashSet<Usage>());

            dataModelLoadBalancer = mapper.map(loadBalancer, org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer.class);
        }

        @Test
        public void should_not_fail_when_domain_loadBalancer_is_empty() {
            loadBalancer = new LoadBalancer();
            try {
                dataModelLoadBalancer = mapper.map(loadBalancer, org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer.class);
            } catch (Exception e) {
                Assert.fail("Empty domain load balancer caused this exception");
            }
        }

        @Test
        public void should_map_access_lists_to_null() {
            Assert.assertNull(dataModelLoadBalancer.getAccessList());
        }

        @Test
        public void should_map_nodes_to_null() {
            Assert.assertNull(dataModelLoadBalancer.getNodes());
        }

        @Test
        public void should_map_virtual_ips_to_null() {
            Assert.assertNull(dataModelLoadBalancer.getVirtualIps());
        }

        @Test
        public void should_map_usage_to_null() {
            Assert.assertNull(dataModelLoadBalancer.getLoadBalancerUsage());
        }
    }

    public static class When_mapping_a_load_balancer_from_domain_to_an_access_list_in_datamodel {

        private DozerBeanMapper mapper;
        private LoadBalancer loadBalancer;
        private AccessList dataModelAccessList;

        @Before
        public void standUp() {
            mapper = MapperBuilder.getConfiguredMapper(publicDozerConfigFile);

            loadBalancer = new LoadBalancer();
            loadBalancer.setCreated(new GregorianCalendar(2010, 10, 10));
            loadBalancer.setUpdated(new GregorianCalendar(2010, 10, 10));
            loadBalancer.setId(100);
            loadBalancer.setName("LB 1");
            loadBalancer.setPort(9999);
            loadBalancer.setAlgorithm(LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN);
            loadBalancer.setStatus(LoadBalancerStatus.PENDING_UPDATE);
            loadBalancer.setProtocol(LoadBalancerProtocol.IMAPv4);
            loadBalancer.setConnectionLogging(true);

            final Set<org.openstack.atlas.service.domain.entities.AccessList> accessLists = new HashSet<org.openstack.atlas.service.domain.entities.AccessList>();
            org.openstack.atlas.service.domain.entities.AccessList accessList1 = new org.openstack.atlas.service.domain.entities.AccessList();
            org.openstack.atlas.service.domain.entities.AccessList accessList2 = new org.openstack.atlas.service.domain.entities.AccessList();
            accessList1.setIpAddress("ip1");
            accessList2.setIpAddress("ip2");
            accessList1.setType(AccessListType.ALLOW);
            accessList2.setType(AccessListType.DENY);
            loadBalancer.setAccessLists(accessLists);

            dataModelAccessList = mapper.map(loadBalancer, AccessList.class);
        }

        @Test
        public void should_not_fail_when_domain_loadbalancer_is_empty() {
            loadBalancer = new LoadBalancer();
            try {
                dataModelAccessList = mapper.map(loadBalancer, AccessList.class);
            } catch (Exception e) {
                Assert.fail("Empty domain load balancer caused this exception");
            }
        }

        @Test
        public void should_map_access_list_items_to_network_items_correctly() {
            for (NetworkItem networkItem : dataModelAccessList.getNetworkItems()) {
                if (!(networkItem.getAddress().equals("ip1") || networkItem.getAddress().equals("ip2"))) {
                    Assert.fail("Did not map the ip address of the access list item correctly");
                }
                if (!(networkItem.getIpVersion().equals(org.openstack.atlas.docs.loadbalancers.api.v1.IpVersion.IPV4) || networkItem.getIpVersion().equals(org.openstack.atlas.docs.loadbalancers.api.v1.IpVersion.IPV6))) {
                    Assert.fail("Did not map the ip version of the access list item correctly");
                }
                if (!(networkItem.getType().equals(NetworkItemType.ALLOW) || networkItem.getType().equals(NetworkItemType.DENY))) {
                    Assert.fail("Did not map the access type of the access list item correctly");
                }
            }
        }
    }



    public static class When_mapping_a_deleted_domain_loadbalancer_to_a_datamodel_loadbalancer {

        private DozerBeanMapper mapper;
        private LoadBalancer loadBalancer;
        private org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer dataModelLb;

        @Before
        public void setUp() {
            mapper = MapperBuilder.getConfiguredMapper(publicDozerConfigFile);
            loadBalancer = createHydratedLoadbalancer();
            loadBalancer.setStatus(LoadBalancerStatus.DELETED);
            dataModelLb = mapper.map(loadBalancer, org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer.class, "DELETED_LB");
        }

        @Test
        public void shouldMapCorrectlyWhenGivenAFullyHydratedLoadbalancer() {
            Assert.assertEquals(new Integer(100), dataModelLb.getId());
            Assert.assertEquals("LB 1", dataModelLb.getName());
            Assert.assertEquals("DELETED", dataModelLb.getStatus());
            Assert.assertEquals(new GregorianCalendar(2010, 10, 10), dataModelLb.getCreated().getTime());
            Assert.assertEquals(new GregorianCalendar(2010, 10, 10), dataModelLb.getUpdated().getTime());
            Assert.assertNull(dataModelLb.getAlgorithm());
            Assert.assertNull(dataModelLb.getCluster());
            Assert.assertNull(dataModelLb.getConnectionThrottle());
            Assert.assertNull(dataModelLb.getHealthMonitor());
            Assert.assertNull(dataModelLb.getProtocol());
            Assert.assertNull(dataModelLb.getPort());
            Assert.assertNull(dataModelLb.getSessionPersistence());
        }

        @Test(expected = org.dozer.MappingException.class)
        public void shouldThrowExceptionWhenMappingFromDataModelToDomainModel() {
            mapper.map(dataModelLb, LoadBalancer.class, "DELETED_LB");
        }

        @Test
        public void shouldNotMapFieldWhenTheyAreNull() {
            loadBalancer.setId(null);
            loadBalancer.setName(null);
            loadBalancer.setStatus(null);
            loadBalancer.setCreated(null);
            loadBalancer.setUpdated(null);
            dataModelLb = mapper.map(loadBalancer, org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer.class, "DELETED_LB");

            Assert.assertNull(dataModelLb.getId());
            Assert.assertNull(dataModelLb.getName());
            Assert.assertNull(dataModelLb.getStatus());
            Assert.assertNull(dataModelLb.getCreated());
            Assert.assertNull(dataModelLb.getUpdated());
        }

        @Test
        public void should_not_fail_when_domain_loadbalancer_is_empty() {
            loadBalancer = new LoadBalancer();
            try {
                dataModelLb = mapper.map(loadBalancer, org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer.class, "DELETED_LB");
            } catch (Exception e) {
                Assert.fail("Empty domain load balancer caused this exception");
            }
        }
    }

    public static class When_mapping_a_hydrated_domain_loadbalancer_to_a_simple_datamodel_loadbalancer {

        private DozerBeanMapper mapper;
        private LoadBalancer loadBalancer;
        private org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer dataModelLb;

        @Before
        public void setUp() {
            mapper = MapperBuilder.getConfiguredMapper(publicDozerConfigFile);
            loadBalancer = createHydratedLoadbalancer();

            loadBalancer.setStatus(LoadBalancerStatus.ACTIVE);
            dataModelLb = mapper.map(loadBalancer, org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer.class, "SIMPLE_LB");
        }

        @Test
        public void shouldMapCorrectlyWhenGivenAFullyHydratedLoadbalancer() {
            Assert.assertEquals(new Integer(100), dataModelLb.getId());
            Assert.assertEquals("LB 1", dataModelLb.getName());
            Assert.assertEquals(LoadBalancerStatus.ACTIVE.name(), dataModelLb.getStatus());
            Assert.assertEquals("WEIGHTED_ROUND_ROBIN", dataModelLb.getAlgorithm());
            Assert.assertEquals("IMAPv4", dataModelLb.getProtocol());
            Assert.assertEquals(new Integer(9999), dataModelLb.getPort());
            Assert.assertNotNull(dataModelLb.getCreated());
            Assert.assertNotNull(dataModelLb.getUpdated());
            Assert.assertNull(dataModelLb.getCluster());
            Assert.assertNull(dataModelLb.getConnectionThrottle());
            Assert.assertNull(dataModelLb.getHealthMonitor());
            Assert.assertNull(dataModelLb.getSessionPersistence());
        }

        @Test(expected = org.dozer.MappingException.class)
        public void shouldThrowExceptionWhenMappingFromDataModelToDomainModel() {
            mapper.map(dataModelLb, LoadBalancer.class, "SIMPLE_LB");
        }

        @Test
        public void shouldMapNodeCountWhenNodesAreAvailable() {
            Assert.assertEquals(loadBalancer.getNodes().size(), dataModelLb.getNodeCount().intValue());
        }

        @Test
        public void shouldNotMapFieldWhenTheyAreNull() {
            loadBalancer.setId(null);
            loadBalancer.setName(null);
            dataModelLb = mapper.map(loadBalancer, org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer.class, "SIMPLE_LB");

            Assert.assertNull(dataModelLb.getId());
            Assert.assertNull(dataModelLb.getName());
        }

        @Test
        public void should_not_fail_when_domain_loadbalancer_is_empty() {
            loadBalancer = new LoadBalancer();
            try {
                dataModelLb = mapper.map(loadBalancer, org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer.class, "SIMPLE_LB");
            } catch (Exception e) {
                Assert.fail("Empty domain load balancer caused this exception");
            }
        }
    }

    public static class When_mapping_a_deleted_domain_loadbalancer_to_a_simple_datamodel_loadbalancer {

        private DozerBeanMapper mapper;
        private LoadBalancer loadBalancer;
        private org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer dataModelLb;

        @Before
        public void setUp() {
            mapper = MapperBuilder.getConfiguredMapper(publicDozerConfigFile);
            loadBalancer = createHydratedLoadbalancer();

            loadBalancer.setStatus(LoadBalancerStatus.DELETED);
            dataModelLb = mapper.map(loadBalancer, org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer.class, "SIMPLE_LB");
        }

        @Test
        public void shouldMapCorrectlyWhenGivenAFullyHydratedLoadbalancer() {
            Assert.assertEquals(new Integer(100), dataModelLb.getId());
            Assert.assertEquals("LB 1", dataModelLb.getName());
            Assert.assertEquals(LoadBalancerStatus.DELETED.name(), dataModelLb.getStatus());
            Assert.assertEquals("WEIGHTED_ROUND_ROBIN", dataModelLb.getAlgorithm());
            Assert.assertEquals("IMAPv4", dataModelLb.getProtocol());
            Assert.assertEquals(new Integer(9999), dataModelLb.getPort());
            Assert.assertNotNull(dataModelLb.getCreated());
            Assert.assertNotNull(dataModelLb.getUpdated());
            Assert.assertNull(dataModelLb.getCluster());
            Assert.assertNull(dataModelLb.getConnectionThrottle());
            Assert.assertNull(dataModelLb.getHealthMonitor());
            Assert.assertNull(dataModelLb.getSessionPersistence());
        }

        @Test(expected = org.dozer.MappingException.class)
        public void shouldThrowExceptionWhenMappingFromDataModelToDomainModel() {
            mapper.map(dataModelLb, LoadBalancer.class, "SIMPLE_LB");
        }

        @Test
        public void shouldNotMapFieldWhenTheyAreNull() {
            loadBalancer.setId(null);
            loadBalancer.setName(null);
            dataModelLb = mapper.map(loadBalancer, org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer.class, "SIMPLE_LB");

            Assert.assertNull(dataModelLb.getId());
            Assert.assertNull(dataModelLb.getName());
        }

        @Test
        public void should_not_fail_when_domain_loadbalancer_is_empty() {
            loadBalancer = new LoadBalancer();
            try {
                dataModelLb = mapper.map(loadBalancer, org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer.class, "SIMPLE_LB");
            } catch (Exception e) {
                Assert.fail("Empty domain load balancer caused this exception");
            }
        }
    }

    private static LoadBalancer createHydratedLoadbalancer() {
        LoadBalancer loadBalancer = new LoadBalancer();
        loadBalancer.setCreated(new GregorianCalendar(2010, 10, 10));
        loadBalancer.setUpdated(new GregorianCalendar(2010, 10, 10));
        loadBalancer.setProvisioned(new GregorianCalendar(2010, 10, 10));
        loadBalancer.setId(100);
        loadBalancer.setName("LB 1");
        loadBalancer.setPort(9999);
        loadBalancer.setHalfClosed(true);

        loadBalancer.setAlgorithm(LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN);
        loadBalancer.setStatus(LoadBalancerStatus.PENDING_UPDATE);
        loadBalancer.setProtocol(LoadBalancerProtocol.IMAPv4);
        loadBalancer.setConnectionLogging(true);

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

        final Set<Node> hashSet = new HashSet<Node>();
        final Node node1 = new Node();
        node1.setCondition(NodeCondition.ENABLED);
        node1.setId(100);
        node1.setIpAddress("ip1");
        node1.setPort(1000);
        node1.setStatus(NodeStatus.ONLINE);
        final Node node2 = new Node();
        node2.setCondition(NodeCondition.DISABLED);
        node2.setId(101);
        node2.setIpAddress("ip2");
        node2.setPort(1001);
        node2.setStatus(NodeStatus.OFFLINE);
        hashSet.add(node1);
        hashSet.add(node2);
        loadBalancer.setNodes(hashSet);

        final Set<VirtualIp> virtualIpSet = new HashSet<VirtualIp>();
        final VirtualIp virtualIp1 = new VirtualIp();
        virtualIp1.setId(100);
        virtualIp1.setIpAddress("ip1");
        virtualIp1.setVipType(VirtualIpType.PUBLIC);
        LoadBalancerJoinVip loadBalancerJoinVip1 = new LoadBalancerJoinVip();
        loadBalancerJoinVip1.setVirtualIp(virtualIp1);
        loadBalancer.getLoadBalancerJoinVipSet().add(loadBalancerJoinVip1);
        final VirtualIp virtualIp2 = new VirtualIp();
        virtualIp2.setId(101);
        virtualIp2.setIpAddress("ip2");
        virtualIp2.setVipType(VirtualIpType.SERVICENET);
        LoadBalancerJoinVip loadBalancerJoinVip2 = new LoadBalancerJoinVip();
        loadBalancerJoinVip2.setVirtualIp(virtualIp2);
        loadBalancer.getLoadBalancerJoinVipSet().add(loadBalancerJoinVip2);

        Cluster cluster = new Cluster();
        cluster.setId(1);
        cluster.setClusterIpv6Cidr("ffff:ffff:ffff:ffff::/64");
        final VirtualIpv6 virtualIp3 = new VirtualIpv6();
        virtualIp3.setId(1);
        virtualIp3.setAccountId(1234);
        virtualIp3.setVipOctets(1);
        virtualIp3.setCluster(cluster);
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

        return loadBalancer;
    }
}
