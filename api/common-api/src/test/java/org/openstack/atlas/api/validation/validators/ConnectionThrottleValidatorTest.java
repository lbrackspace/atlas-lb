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
        public void shouldRejectWhenMissingAllAttributes() {
            assertFalse(validator.validate(new ConnectionThrottle(), PUT).passedValidation());
        }

        @Test
        public void shouldAcceptWhenOnlyMinConnectionsIsSet() {
            connectionLimits.setMaxConnections(null);
            connectionLimits.setMaxConnectionRate(null);
            connectionLimits.setRateInterval(null);
            assertTrue(validator.validate(connectionLimits, PUT).passedValidation());
        }

        @Test
        public void shouldAcceptWhenOnlyMaxConnectionsIsSet() {
            connectionLimits.setMinConnections(null);
            connectionLimits.setMaxConnectionRate(null);
            connectionLimits.setRateInterval(null);
            assertTrue(validator.validate(connectionLimits, PUT).passedValidation());
        }

        @Test
        public void shouldAcceptWhenOnlyMaxConnectionRateIsSet() {
            connectionLimits.setMinConnections(null);
            connectionLimits.setMaxConnections(null);
            connectionLimits.setRateInterval(null);
            assertTrue(validator.validate(connectionLimits, PUT).passedValidation());
        }

        @Test
        public void shouldAcceptWhenOnlyRateIntervalIsSet() {
            connectionLimits.setMinConnections(null);
            connectionLimits.setMaxConnections(null);
            connectionLimits.setMaxConnectionRate(null);
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
        public void shouldRejectInvalidMaxConnectionsRangeMin() {
            connectionLimits.setMaxConnections(-1);
            assertFalse(validator.validate(connectionLimits, PUT).passedValidation());
        }

        @Test
        public void shouldRejectInvalidMaxConnectionsRangeMax() {
            connectionLimits.setMaxConnections(100001);
            assertFalse(validator.validate(connectionLimits, PUT).passedValidation());
        }

        @Test
        public void shouldRejectInvalidMaxConnectionRateRangeMin() {
            connectionLimits.setMaxConnectionRate(-1);
            assertFalse(validator.validate(connectionLimits, PUT).passedValidation());
        }

        @Test
        public void shouldRejectInvalidMaxConnectionRateRangeMax() {
            connectionLimits.setMaxConnectionRate(100001);
            assertFalse(validator.validate(connectionLimits, PUT).passedValidation());
        }

        @Test
        public void shouldRejectInvalidRateIntervalMin() {
            connectionLimits.setRateInterval(0);
            assertFalse(validator.validate(connectionLimits, PUT).passedValidation());
        }

        @Test
        public void shouldRejectInvalidRateIntervalMax() {
            connectionLimits.setRateInterval(3601);
            assertFalse(validator.validate(connectionLimits, PUT).passedValidation());
        }
    }
}
