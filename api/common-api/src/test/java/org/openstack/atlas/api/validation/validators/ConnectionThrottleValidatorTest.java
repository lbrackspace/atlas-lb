package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionThrottle;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class ConnectionThrottleValidatorTest {

    public static class WhenValidatingPut {
        private ConnectionThrottle connectionLimits;
        private ConnectionThrottleValidator validator;

        @Before
        public void setupValidConnectionLimitsForPut() {
            validator = new ConnectionThrottleValidator();

            connectionLimits = new ConnectionThrottle();
            connectionLimits.setMinConnections(10);
            connectionLimits.setMaxConnections(100);
            connectionLimits.setMaxConnectionRate(50);
            connectionLimits.setRateInterval(60);
        }

        @Test
        public void shouldAcceptValidConnectionLimits() {
            assertTrue(validator.validate(connectionLimits, PUT).passedValidation());
        }

        @Test
        public void shouldRejectWhenMissingMaxConnectionsAttributesBecauseTheOthersAreDeprecated() {
            assertFalse(validator.validate(new ConnectionThrottle(), PUT).passedValidation());
        }

        @Test
        public void shouldAcceptIfMaxConnectionsIsSet() {
            connectionLimits.setMinConnections(null);
            connectionLimits.setMaxConnectionRate(null);
            connectionLimits.setRateInterval(null);
            assertTrue(validator.validate(connectionLimits, PUT).passedValidation());
        }

        @Test
        public void shouldRejectInvalidMinConnectionsRangeMin() {
            connectionLimits.setMinConnections(-1);
            assertFalse(validator.validate(connectionLimits, PUT).passedValidation());
        }

        @Test
        public void shouldRejectInvalidMinConnectionsRangeMax() {
            connectionLimits.setMinConnections(1001);
            assertFalse(validator.validate(connectionLimits, PUT).passedValidation());
        }

        @Test
        public void shouldRejectInvalidMaxConnectionsRangeMinEvenThoughItsDeprecated() {
            connectionLimits.setMaxConnections(-1);
            assertFalse(validator.validate(connectionLimits, PUT).passedValidation());
        }

        @Test
        public void shouldRejectInvalidMaxConnectionsRangeMaxEvenThoughItsDeprecated() {
            connectionLimits.setMaxConnections(100001);
            assertFalse(validator.validate(connectionLimits, PUT).passedValidation());
        }

        @Test
        public void shouldRejectInvalidMaxConnectionRateRangeMinEvenThoughItsDeprecated() {
            connectionLimits.setMaxConnectionRate(-1);
            assertFalse(validator.validate(connectionLimits, PUT).passedValidation());
        }

        @Test
        public void shouldRejectInvalidMaxConnectionRateRangeMaxEvenThoughItsDeprecated() {
            connectionLimits.setMaxConnectionRate(100001);
            assertFalse(validator.validate(connectionLimits, PUT).passedValidation());
        }

        @Test
        public void shouldRejectInvalidRateIntervalMinEvenThoughItsDeprecated() {
            connectionLimits.setRateInterval(0);
            assertFalse(validator.validate(connectionLimits, PUT).passedValidation());
        }

        @Test
        public void shouldRejectInvalidRateIntervalMaxEvenThoughItsDeprecated() {
            connectionLimits.setRateInterval(3601);
            assertFalse(validator.validate(connectionLimits, PUT).passedValidation());
        }
    }
}
