package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.v1.IpVersion;
import org.openstack.atlas.docs.loadbalancers.api.v1.VipType;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.VirtualIpContext.POST_IPV6;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class VirtualIpValidatorTest {

    public static class WhenValidatingHttpPostContext {

        private VirtualIp vip;
        private VirtualIpValidator validator;

        @Before
        public void setup() {
            validator = new VirtualIpValidator();
            vip = new VirtualIp();
        }

        @Test
        public void shouldRejectIfAllAttributesAreNull() {
            assertFalse(validator.validate(vip, POST).passedValidation());
        }

        @Test
        public void shouldRejectVipAddress() {
            vip.setAddress("10.10.10.10");
            assertFalse(validator.validate(vip, POST).passedValidation());
        }

        @Test
        public void shouldAcceptIdOnly() {
            vip.setId(1234);
            assertTrue(validator.validate(vip, POST).passedValidation());
        }

        @Test
        public void shouldAcceptTypeOnly() {
            vip.setType(VipType.PUBLIC);
            assertTrue(validator.validate(vip, POST).passedValidation());
        }

        @Test
        public void shouldAcceptTypeAndIpVersion() {
            vip.setType(VipType.PUBLIC);
            vip.setIpVersion(IpVersion.IPV4);
            assertTrue(validator.validate(vip, POST).passedValidation());
        }

        @Test
        public void shouldRejectIdWithTypeAndIpVersion() {
            vip.setId(1234);
            vip.setType(VipType.PUBLIC);
            vip.setIpVersion(IpVersion.IPV4);
            assertFalse(validator.validate(vip, POST).passedValidation());
        }

        @Test
        public void shouldRejectIdWithType() {
            vip.setId(1234);
            vip.setType(VipType.PUBLIC);
            assertFalse(validator.validate(vip, POST).passedValidation());
        }

        @Test
        public void shouldRejectIdWithIpVersion() {
            vip.setId(1234);
            vip.setIpVersion(IpVersion.IPV4);
            assertFalse(validator.validate(vip, POST).passedValidation());
        }
    }

    public static class WhenValidatingPostIpv6Context {

        private VirtualIp vip;
        private VirtualIpValidator validator;

        @Before
        public void setup() {
            validator = new VirtualIpValidator();
            vip = new VirtualIp();
        }

        @Test
        public void shouldRejectIfAllAttributesAreNull() {
            assertFalse(validator.validate(vip, POST_IPV6).passedValidation());
        }

        @Test
        public void shouldRejectVipAddress() {
            vip.setAddress("10.10.10.10");
            assertFalse(validator.validate(vip, POST_IPV6).passedValidation());
        }

        @Test
        public void shouldNotAcceptIdOnly() {
            vip.setId(1234);
            assertFalse(validator.validate(vip, POST_IPV6).passedValidation());
        }

        @Test
        public void shouldRejectTypeOnly() {
            vip.setType(VipType.PUBLIC);
            assertFalse(validator.validate(vip, POST_IPV6).passedValidation());
        }

        @Test
        public void shouldAcceptPublicTypeAndIpv6Version() {
            vip.setType(VipType.PUBLIC);
            vip.setIpVersion(IpVersion.IPV6);
            assertTrue(validator.validate(vip, POST_IPV6).passedValidation());
        }

        @Test
        public void shouldRejectServicenetTypeAndIpv4Version() {
            vip.setType(VipType.SERVICENET);
            vip.setIpVersion(IpVersion.IPV4);
            assertFalse(validator.validate(vip, POST_IPV6).passedValidation());
        }

        @Test
        public void shouldRejectPublicTypeAndIpv4Version() {
            vip.setType(VipType.PUBLIC);
            vip.setIpVersion(IpVersion.IPV4);
            assertFalse(validator.validate(vip, POST_IPV6).passedValidation());
        }

        @Test
        public void shouldRejectServicenetTypeAndIpv6Version() {
            vip.setType(VipType.SERVICENET);
            vip.setIpVersion(IpVersion.IPV6);
            assertFalse(validator.validate(vip, POST_IPV6).passedValidation());
        }

        @Test
        public void shouldRejectIdWithAllTypesAndAllIpVersions() {
            vip.setId(1234);

            for (VipType vipType : VipType.values()) {
                for (IpVersion ipVersion : IpVersion.values()) {
                    vip.setType(vipType);
                    vip.setIpVersion(ipVersion);
                    assertFalse(validator.validate(vip, POST_IPV6).passedValidation());
                }
            }
        }

        @Test
        public void shouldRejectIdWithAllTypes() {
            vip.setId(1234);

            for (VipType vipType : VipType.values()) {
                vip.setType(vipType);
                assertFalse(validator.validate(vip, POST_IPV6).passedValidation());
            }
        }
    }
}