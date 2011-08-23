package org.openstack.atlas.api.resources;

import junit.framework.Assert;
import org.dozer.DozerBeanMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.docs.loadbalancers.api.v1.Created;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.v1.VipType;
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entities.VirtualIpType;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(Enclosed.class)
public class VirtualIpsResourceTest {

    public static class whenMappingVirtualIps {

        private DozerBeanMapper mapper;
        private LoadBalancer loadBalancer;
        private org.openstack.atlas.service.domain.entities.LoadBalancer domainLoadBalancer;

        @Before
        public void setUp() {
            List<String> mappingFiles = new ArrayList<String>();
            mappingFiles.add("loadbalancing-dozer-mapping.xml");
            mapper = new DozerBeanMapper(mappingFiles);

            loadBalancer = new LoadBalancer();
            Created created = new Created();
            created.setTime(new GregorianCalendar(2010, 10, 10));
            loadBalancer.setCreated(created);
            loadBalancer.setId(100);
            loadBalancer.setName("LB 1");
            loadBalancer.setPort(9999);


            org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp virtualIp1 = new org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp();
            virtualIp1.setAddress("10.10.10.1");
            virtualIp1.setId(2000);
            virtualIp1.setType(VipType.PUBLIC);

            List<org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp> virtualIps = new ArrayList<org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp>();
            virtualIps.add(virtualIp1);
            loadBalancer.getVirtualIps().addAll(virtualIps);

            domainLoadBalancer = mapper.map(loadBalancer, org.openstack.atlas.service.domain.entities.LoadBalancer.class);
        }

        @Test
        public void should_map_virtualips_on_the_loadbalancer() {
            Assert.assertEquals(1, domainLoadBalancer.getLoadBalancerJoinVipSet().size());
        }

        @Ignore // TODO : Temporary mitigation for SITESLB-1519
        @Test
        public void should_map_correct_virtualip_elements_on_loadbalancer() {
            Assert.assertEquals(new Integer(100), domainLoadBalancer.getId());
            for (LoadBalancerJoinVip loadBalancerJoinVip : domainLoadBalancer.getLoadBalancerJoinVipSet()) {
                if (loadBalancerJoinVip.getVirtualIp().getId() != 2000)
                    Assert.fail("The id was not mapped correctly");
                if (loadBalancerJoinVip.getVirtualIp().getIpAddress() != "10.10.10.1")
                    Assert.fail("The ip address was not mapped correctly");
                if (loadBalancerJoinVip.getVirtualIp().getVipType() != forTestPurposes(VipType.PUBLIC))
                    Assert.fail("The vipType was not mapped correctly");
            }

        }

        public static class WhenRetrievingResources {
            private VirtualIpsResource virtualIpsResource;

            @Before
            public void setUp() {
                virtualIpsResource = new VirtualIpsResource();
            }

            @Test
            public void shouldSetAccountIdLbIdAndVipIdForVirtualIpResource() {
                VirtualIpResource virtualIpResource = mock(VirtualIpResource.class);
                virtualIpsResource.setVirtualIpResource(virtualIpResource);
                virtualIpsResource.retrieveVirtualIpResource(anyInt());
                verify(virtualIpResource).setAccountId(anyInt());
                verify(virtualIpResource).setLoadBalancerId(anyInt());
                verify(virtualIpResource).setId(anyInt());
            }
        }

        public static VirtualIpType forTestPurposes(VipType virtualIpType) {
            org.openstack.atlas.service.domain.entities.VirtualIpType newIpType = null;
            if (virtualIpType == null) {
                return null;
            }
            switch (virtualIpType) {
                case PUBLIC:
                    newIpType = org.openstack.atlas.service.domain.entities.VirtualIpType.PUBLIC;
                    break;
                case SERVICENET:
                    newIpType = org.openstack.atlas.service.domain.entities.VirtualIpType.SERVICENET;
                    break;
            }
            return newIpType;
        }
    }
}
