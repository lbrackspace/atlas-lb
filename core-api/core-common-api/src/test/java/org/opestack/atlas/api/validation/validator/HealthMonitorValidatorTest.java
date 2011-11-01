package org.opestack.atlas.api.validation.validator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.HealthMonitorValidator;
import org.openstack.atlas.api.validation.validator.builder.ConnectMonitorValidatorBuilder;
import org.openstack.atlas.api.validation.validator.builder.HealthMonitorValidatorBuilder;
import org.openstack.atlas.api.validation.validator.builder.HttpMonitorValidatorBuilder;
import org.openstack.atlas.core.api.v1.HealthMonitor;
import org.openstack.atlas.service.domain.stub.StubFactory;

import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;

@RunWith(Enclosed.class)
public class HealthMonitorValidatorTest {

    public static class WhenValidatingPutContextForConnectMonitor {
        private HealthMonitor healthMonitor;
        private HealthMonitorValidator validator;

        @Before
        public void standUp() {
            validator = new HealthMonitorValidator(
                    new HealthMonitorValidatorBuilder(
                            new ConnectMonitorValidatorBuilder(),
                            new HttpMonitorValidatorBuilder()));
            healthMonitor = StubFactory.createHydratedDataModelConnectMonitorForPut();
        }

        @Test
        public void shouldPassValidationWhenGivenAValidMonitor() {
            ValidatorResult result = validator.validate(healthMonitor, PUT);
            Assert.assertTrue(result.passedValidation());
        }

        @Test
        public void shouldPassValidationWhenSpecifyingOnlyDelay() {
            healthMonitor.setTimeout(null);
            healthMonitor.setAttemptsBeforeDeactivation(null);
            ValidatorResult result = validator.validate(healthMonitor, PUT);
            Assert.assertTrue(result.passedValidation());
        }

        @Test
        public void shouldPassValidationWhenSpecifyingOnlyTimeout() {
            healthMonitor.setDelay(null);
            healthMonitor.setAttemptsBeforeDeactivation(null);
            ValidatorResult result = validator.validate(healthMonitor, PUT);
            Assert.assertTrue(result.passedValidation());
        }

        @Test
        public void shouldPassValidationWhenSpecifyingOnlyAttempts() {
            healthMonitor.setDelay(null);
            healthMonitor.setTimeout(null);
            ValidatorResult result = validator.validate(healthMonitor, PUT);
            Assert.assertTrue(result.passedValidation());
        }

        @Test
        public void shouldFailValidationWhenPassingInANullHealthMonitor() {
            ValidatorResult result = validator.validate(null, PUT);
            Assert.assertFalse(result.passedValidation());
        }

        @Test
        public void shouldFailValidationWhenPassingInAnEmptyHealthMonitor() {
            ValidatorResult result = validator.validate(new HealthMonitor(), PUT);
            Assert.assertFalse(result.passedValidation());
        }

        @Test
        public void shouldFailValidationWhenNoMainAttributesSet() {
            healthMonitor.setDelay(null);
            healthMonitor.setTimeout(null);
            healthMonitor.setAttemptsBeforeDeactivation(null);
            ValidatorResult result = validator.validate(new HealthMonitor(), PUT);
            Assert.assertFalse(result.passedValidation());
        }

        @Test
        public void shouldFailValidationWhenSpecifyingPath() {
            healthMonitor.setPath("/this/should/not/be/set");
            ValidatorResult result = validator.validate(healthMonitor, PUT);
            Assert.assertFalse(result.passedValidation());
        }
    }

    public static class WhenValidatingPutContextForHttpMonitor {
        private HealthMonitor healthMonitor;
        private HealthMonitorValidator validator;

        @Before
        public void standUp() {
            validator = new HealthMonitorValidator(
                    new HealthMonitorValidatorBuilder(
                            new ConnectMonitorValidatorBuilder(),
                            new HttpMonitorValidatorBuilder()));
            healthMonitor = StubFactory.createHydratedDataModelHttpMonitorForPut();
        }

        @Test
        public void shouldPassValidationWhenGivenAValidMonitor() {
            ValidatorResult result = validator.validate(healthMonitor, PUT);
            Assert.assertTrue(result.passedValidation());
        }

        @Test
        public void shouldPassValidationWhenSpecifyingOnlyDelay() {
            healthMonitor.setTimeout(null);
            healthMonitor.setAttemptsBeforeDeactivation(null);
            healthMonitor.setPath(null);
            ValidatorResult result = validator.validate(healthMonitor, PUT);
            Assert.assertTrue(result.passedValidation());
        }

        @Test
        public void shouldPassValidationWhenSpecifyingOnlyTimeout() {
            healthMonitor.setDelay(null);
            healthMonitor.setAttemptsBeforeDeactivation(null);
            healthMonitor.setPath(null);
            ValidatorResult result = validator.validate(healthMonitor, PUT);
            Assert.assertTrue(result.passedValidation());
        }

        @Test
        public void shouldPassValidationWhenSpecifyingOnlyAttempts() {
            healthMonitor.setDelay(null);
            healthMonitor.setTimeout(null);
            healthMonitor.setPath(null);
            ValidatorResult result = validator.validate(healthMonitor, PUT);
            Assert.assertTrue(result.passedValidation());
        }

        @Test
        public void shouldPassValidationWhenSpecifyingOnlyPath() {
            healthMonitor.setDelay(null);
            healthMonitor.setTimeout(null);
            healthMonitor.setAttemptsBeforeDeactivation(null);
            ValidatorResult result = validator.validate(healthMonitor, PUT);
            Assert.assertTrue(result.passedValidation());
        }

        @Test
        public void shouldFailValidationWhenPassingInANullHealthMonitor() {
            ValidatorResult result = validator.validate(null, PUT);
            Assert.assertFalse(result.passedValidation());
        }

        @Test
        public void shouldFailValidationWhenPassingInAnEmptyHealthMonitor() {
            ValidatorResult result = validator.validate(new HealthMonitor(), PUT);
            Assert.assertFalse(result.passedValidation());
        }

        @Test
        public void shouldFailValidationWhenNoMainAttributesSet() {
            healthMonitor.setDelay(null);
            healthMonitor.setTimeout(null);
            healthMonitor.setAttemptsBeforeDeactivation(null);
            healthMonitor.setPath(null);
            ValidatorResult result = validator.validate(healthMonitor, PUT);
            Assert.assertFalse(result.passedValidation());
        }
        
    }
}
