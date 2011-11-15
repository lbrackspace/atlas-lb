package org.openstack.atlas.rax.api.validation.validator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.VirtualIpValidator;
import org.openstack.atlas.core.api.v1.IpVersion;
import org.openstack.atlas.core.api.v1.VipType;
import org.openstack.atlas.core.api.v1.VirtualIp;
import org.openstack.atlas.rax.api.validation.validator.builder.RaxVirtualIpValidatorBuilder;

import static org.openstack.atlas.rax.api.validation.context.VirtualIpContext.POST_IPV6;

@RunWith(Enclosed.class)
public class RaxVirtualIpValidatorTest {

    public static class WhenValidatingPostContext {
        private VirtualIp virtualIp;
        private VirtualIpValidator validator;

        @Before
        public void standUp() {
            validator = new VirtualIpValidator(new RaxVirtualIpValidatorBuilder());
            virtualIp = new VirtualIp();
            virtualIp.setIpVersion(IpVersion.IPV6);
            virtualIp.setType(VipType.PUBLIC);
        }

        @Test
        public void shouldPassValidationWhenGivenAProperVirtualIp() {
            ValidatorResult result = validator.validate(virtualIp, POST_IPV6);
            Assert.assertTrue(result.passedValidation());
        }

        @Test
        public void shouldFailValidationWhenNoAttributesSet() {
            virtualIp = new VirtualIp();
            ValidatorResult result = validator.validate(virtualIp, POST_IPV6);
            Assert.assertFalse(result.passedValidation());
        }

        @Test
        public void shouldFailWhenPassingInNull() {
            virtualIp = null;
            ValidatorResult result = validator.validate(virtualIp, POST_IPV6);
            Assert.assertFalse(result.passedValidation());
        }

        @Test
        public void shouldFailValidationWhenOnlySpecifyingIpVersion() {
            virtualIp.setType(null);
            ValidatorResult result = validator.validate(virtualIp, POST_IPV6);
            Assert.assertFalse(result.passedValidation());
        }

        @Test
        public void shouldFailValidationWhenOnlySpecifyingType() {
            virtualIp.setIpVersion(null);
            ValidatorResult result = validator.validate(virtualIp, POST_IPV6);
            Assert.assertFalse(result.passedValidation());
        }

        @Test
        public void shouldFailValidationWhenSpecifyingAddress() {
            virtualIp.setAddress("1.1.1.1");
            ValidatorResult result = validator.validate(virtualIp, POST_IPV6);
            Assert.assertFalse(result.passedValidation());
        }

        @Test
        public void shouldFailValidationWhenGivenASharedVip() {
            virtualIp.setId(1234);
            ValidatorResult result = validator.validate(virtualIp, POST_IPV6);
            Assert.assertFalse(result.passedValidation());
        }

    }
}
