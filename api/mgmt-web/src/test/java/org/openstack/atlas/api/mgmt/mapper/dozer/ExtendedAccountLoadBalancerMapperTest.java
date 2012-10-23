package org.openstack.atlas.api.mgmt.mapper.dozer;

import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ExtendedAccountLoadbalancer;
import org.openstack.atlas.docs.loadbalancers.api.v1.VipType;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.pojos.ExtendedAccountLoadBalancer;

import java.util.Arrays;
import java.util.Set;

@RunWith(Enclosed.class)
public class ExtendedAccountLoadBalancerMapperTest {

    private static final String managementDozerConfigFile = "loadbalancing-dozer-management-mapping.xml";

    public static class WhenMappingExtendedAccountLoadBalancer {

        private ExtendedAccountLoadBalancer ealb;
        private Set<LoadBalancerJoinVip> loadBalancerJoinVipSet;
        private Set<LoadBalancerJoinVip6> loadBalancerJoinVip6Set;

        private LoadBalancer loadBalancer;
        private DozerBeanMapper mapper;

        @Before
        public void setUp() {
            mapper = MapperBuilder.getConfiguredMapper(managementDozerConfigFile);

            loadBalancer = new LoadBalancer();
            loadBalancer.setId(100);
            loadBalancer.setName("LB 1");
            loadBalancer.setPort(9999);

            loadBalancer.setProtocol(LoadBalancerProtocol.UDP);
            loadBalancer.setAlgorithm(LoadBalancerAlgorithm.LEAST_CONNECTIONS);
            loadBalancer.setStatus(LoadBalancerStatus.ACTIVE);

            loadBalancer.setConnectionLogging(true);

            Node node1 = new Node();
            node1.setId(4100);
            node1.setPort(80);
            node1.setCondition(NodeCondition.DISABLED);

            Node node2 = new Node();
            node2.setId(4101);
            node2.setPort(85);
            node2.setCondition(NodeCondition.DRAINING);
            node2.setStatus(NodeStatus.OFFLINE);

            loadBalancer.getNodes().add(node1);
            loadBalancer.getNodes().add(node2);

            VirtualIp virtualIp1 = new VirtualIp();
            virtualIp1.setId(2000);
            virtualIp1.setVipType(VirtualIpType.PUBLIC);

            ealb = new ExtendedAccountLoadBalancer();
            ealb.setClusterId(1234);
            ealb.setLoadBalancerId(1);
            ealb.setClusterName("testCluster");
            ealb.setProtocol("UDP");
            ealb.setRegion(DataCenter.DFW);
            ealb.setStatus("ONLINE");
            ealb.setLoadBalancerName(loadBalancer.getName());

            LoadBalancerJoinVip jv = new LoadBalancerJoinVip(loadBalancer.getPort(), loadBalancer, virtualIp1);
//            LoadBalancerJoinVip6 jv6 = new LoadBalancerJoinVip6(loadBalancer.getPort(), loadBalancer, virtualIp6);
            ealb.getLoadBalancerJoinVipSet().add(jv);

        }

        @Test
        public void shouldMapExtendedAccountLoadBalancer() {
            try {
                ExtendedAccountLoadbalancer ealb1 = mapper.map(ealb, ExtendedAccountLoadbalancer.class, "SIMPLE_VIP_CPLB");
                Assert.assertTrue("ExtendedAccountLoadBalancer classes mapped successfully.", true);
            } catch (Exception e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void shoulMapClusterName() {
            try {
                ExtendedAccountLoadbalancer ealb1 = mapper.map(ealb, ExtendedAccountLoadbalancer.class, "SIMPLE_VIP_CPLB");
                Assert.assertEquals("testCluster", ealb1.getClusterName());
            } catch (Exception e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void shoulMapClusterID() {
            try {
                ExtendedAccountLoadbalancer ealb1 = mapper.map(ealb, ExtendedAccountLoadbalancer.class, "SIMPLE_VIP_CPLB");
                Assert.assertEquals((Integer) 1234, ealb1.getClusterId());
            } catch (Exception e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void shoulMapLoadBalancerID() {
            try {
                ExtendedAccountLoadbalancer ealb1 = mapper.map(ealb, ExtendedAccountLoadbalancer.class, "SIMPLE_VIP_CPLB");
                Assert.assertEquals((Integer) 1, ealb1.getLoadBalancerId());
            } catch (Exception e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void shoulMapLoadBalancerName() {
            try {
                ExtendedAccountLoadbalancer ealb1 = mapper.map(ealb, ExtendedAccountLoadbalancer.class, "SIMPLE_VIP_CPLB");
                Assert.assertEquals("LB 1", ealb1.getLoadBalancerName());
            } catch (Exception e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void shoulMapRegion() {
            try {
                ExtendedAccountLoadbalancer ealb1 = mapper.map(ealb, ExtendedAccountLoadbalancer.class, "SIMPLE_VIP_CPLB");
                Assert.assertEquals(org.openstack.atlas.docs.loadbalancers.api.management.v1.DataCenter.DFW, ealb1.getRegion());
            } catch (Exception e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
                Assert.fail(e.getMessage());
            }
        }


        @Test
        public void shoulMapStatus() {
            try {
                ExtendedAccountLoadbalancer ealb1 = mapper.map(ealb, ExtendedAccountLoadbalancer.class, "SIMPLE_VIP_CPLB");
                Assert.assertEquals("ONLINE", ealb1.getStatus());
            } catch (Exception e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void shoulMapProtocol() {
            try {
                ExtendedAccountLoadbalancer ealb1 = mapper.map(ealb, ExtendedAccountLoadbalancer.class, "SIMPLE_VIP_CPLB");
                Assert.assertEquals("UDP", ealb1.getProtocol());
            } catch (Exception e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void shoulMapVipType() {
            try {
                ExtendedAccountLoadbalancer ealb1 = mapper.map(ealb, ExtendedAccountLoadbalancer.class, "SIMPLE_VIP_CPLB");
                org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp vip = ealb1.getVirtualIps().get(0);
                Assert.assertEquals(VipType.PUBLIC, vip.getType());
            } catch (Exception e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void shoulMapVipVersion() {
            try {
                ExtendedAccountLoadbalancer ealb1 = mapper.map(ealb, ExtendedAccountLoadbalancer.class, "SIMPLE_VIP_CPLB");
                org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp vip = ealb1.getVirtualIps().get(0);
                Assert.assertEquals(org.openstack.atlas.docs.loadbalancers.api.v1.IpVersion.IPV4, vip.getIpVersion());
            } catch (Exception e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void shoulMapVipID() {
            try {
                ExtendedAccountLoadbalancer ealb1 = mapper.map(ealb, ExtendedAccountLoadbalancer.class, "SIMPLE_VIP_CPLB");
                org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp vip = ealb1.getVirtualIps().get(0);
                Assert.assertEquals((Integer)2000, vip.getId());
            } catch (Exception e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
                Assert.fail(e.getMessage());
            }
        }
    }
}