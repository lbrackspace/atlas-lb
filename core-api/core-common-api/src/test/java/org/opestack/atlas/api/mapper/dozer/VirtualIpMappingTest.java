package org.opestack.atlas.api.mapper.dozer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.common.ip.exception.IPStringConversionException1;
import org.openstack.atlas.core.api.v1.IpVersion;
import org.openstack.atlas.core.api.v1.VipType;
import org.openstack.atlas.service.domain.stub.StubFactory;

@RunWith(Enclosed.class)
public class VirtualIpMappingTest {

    public static class WhenMappingAVirtualIpFromApiToDomainIpv4 extends MappingBase {
        private org.openstack.atlas.core.api.v1.VirtualIp apiVirtualIp;
        private org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip domainLbJoinVip;

        @Before
        public void setUp() throws Exception {
            apiVirtualIp = StubFactory.createHydratedDataModelVirtualIp();
            apiVirtualIp.setIpVersion(IpVersion.IPV4);
            domainLbJoinVip = mapper.map(apiVirtualIp, org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip.class);
        }

        @Test
        public void shouldNotFailWhenApiVirtualIpIsEmpty() {
            apiVirtualIp = new org.openstack.atlas.core.api.v1.VirtualIp();
            try {
                domainLbJoinVip = mapper.map(apiVirtualIp, org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip.class);
            } catch (Exception e) {
                Assert.fail("Empty API virtual ip caused this exception");
            }
        }

        @Test
        public void shouldMapAllAttributes() {
            Assert.assertEquals(apiVirtualIp.getId(), domainLbJoinVip.getVirtualIp().getId());
            Assert.assertEquals(apiVirtualIp.getAddress(), domainLbJoinVip.getVirtualIp().getAddress());
            Assert.assertEquals(apiVirtualIp.getType().name(), domainLbJoinVip.getVirtualIp().getVipType().name());
        }
    }

    public static class WhenMappingAVirtualIpFromDomainIpv4ToApi extends MappingBase {
        private org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip domainLbJoinVip;
        private org.openstack.atlas.core.api.v1.VirtualIp apiVirtualIp;

        @Before
        public void setUp() throws Exception {
            domainLbJoinVip = StubFactory.createHydratedLoadBalancerJoinVip();
            apiVirtualIp = mapper.map(domainLbJoinVip, org.openstack.atlas.core.api.v1.VirtualIp.class);
        }

        @Test
        public void shouldNotFailWhenApiVirtualIpIsEmpty() {
            domainLbJoinVip = new org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip();
            try {
                apiVirtualIp = mapper.map(domainLbJoinVip, org.openstack.atlas.core.api.v1.VirtualIp.class);
            } catch (Exception e) {
                Assert.fail("Empty domain virtual ip caused this exception");
            }
        }

        @Test
        public void shouldMapAllAttributes() {
            Assert.assertEquals(domainLbJoinVip.getVirtualIp().getId(), apiVirtualIp.getId());
            Assert.assertEquals(domainLbJoinVip.getVirtualIp().getAddress(), apiVirtualIp.getAddress());
            Assert.assertEquals(domainLbJoinVip.getVirtualIp().getVipType().name(), apiVirtualIp.getType().name());
            Assert.assertEquals(IpVersion.IPV4, apiVirtualIp.getIpVersion());
        }
    }

    public static class WhenMappingAVirtualIpFromDomainIpv6ToApi extends MappingBase {
        private org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip6 domainLbJoinVip;
        private org.openstack.atlas.core.api.v1.VirtualIp apiVirtualIp;

        @Before
        public void setUp() throws Exception {
            domainLbJoinVip = StubFactory.createHydratedDomainVirtualIpv6();
            apiVirtualIp = mapper.map(domainLbJoinVip, org.openstack.atlas.core.api.v1.VirtualIp.class);
        }

        @Test
        public void shouldNotFailWhenDomainVirtualIpIsEmpty() {
            apiVirtualIp = new org.openstack.atlas.core.api.v1.VirtualIp();
            try {
                apiVirtualIp = mapper.map(domainLbJoinVip, org.openstack.atlas.core.api.v1.VirtualIp.class);
            } catch (Exception e) {
                Assert.fail("Empty domain virtual ip (IPV6) caused this exception");
            }
        }

        @Test
        public void shouldMapAllAttributes() throws IPStringConversionException1 {
            Assert.assertEquals(domainLbJoinVip.getVirtualIp().getId(), apiVirtualIp.getId());
            Assert.assertEquals(domainLbJoinVip.getVirtualIp().getDerivedIpString(), apiVirtualIp.getAddress());
            Assert.assertEquals(IpVersion.IPV6, apiVirtualIp.getIpVersion());
            Assert.assertEquals(VipType.PUBLIC, apiVirtualIp.getType());
        }
    }
}
