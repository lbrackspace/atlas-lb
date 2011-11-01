package org.opestack.atlas.api.validation.validator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.ConnectionThrottleValidator;
import org.openstack.atlas.api.validation.validator.builder.ConnectionThrottleValidatorBuilder;
import org.openstack.atlas.core.api.v1.ConnectionThrottle;
import org.openstack.atlas.service.domain.stub.StubFactory;

import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;

@RunWith(Enclosed.class)
public class ConnectionThrottleValidatorTest {

    public static class WhenValidatingPutContextForConnectMonitor {
        private ConnectionThrottle connectionThrottle;
        private ConnectionThrottleValidator validator;

        @Before
        public void standUp() {
            validator = new ConnectionThrottleValidator(new ConnectionThrottleValidatorBuilder());
            connectionThrottle = StubFactory.createHydratedDataModelConnectionThrottle();
        }

        @Test
        public void shouldPassValidationWhenGivenAValidThrottle() {
            ValidatorResult result = validator.validate(connectionThrottle, PUT);
            Assert.assertTrue(result.passedValidation());
        }

        @Test
        public void shouldPassValidationWhenPassingOnlyMaxRequestRate() {
            connectionThrottle.setRateInterval(null);
            ValidatorResult result = validator.validate(connectionThrottle, PUT);
            Assert.assertTrue(result.passedValidation());
        }

        @Test
        public void shouldPassValidationWhenPassingOnlyRateInterval() {
            connectionThrottle.setMaxRequestRate(null);
            ValidatorResult result = validator.validate(connectionThrottle, PUT);
            Assert.assertTrue(result.passedValidation());
        }

        @Test
        public void shouldFailValidationWhenPassingNull() {
            ValidatorResult result = validator.validate(null, PUT);
            Assert.assertFalse(result.passedValidation());
        }

        @Test
        public void shouldFailValidationWhenNoAttributesAreSet() {
            ValidatorResult result = validator.validate(new ConnectionThrottle(), PUT);
            Assert.assertFalse(result.passedValidation());
        }
    }
}
