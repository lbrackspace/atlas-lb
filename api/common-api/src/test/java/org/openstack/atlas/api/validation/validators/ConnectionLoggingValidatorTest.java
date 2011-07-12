package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionLogging;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class ConnectionLoggingValidatorTest {

    public static class whenValidatingPut {
        private ConnectionLogging conLog;
        private ConnectionLoggingValidator validator;

        @Before
        public void setupValidConnectionLogging() {
            validator = new ConnectionLoggingValidator();

            conLog = new ConnectionLogging();
            conLog.setEnabled(true);
        }

        @Test
        public void shouldAcceptValidConnectionLimits() {
            assertTrue(validator.validate(conLog, PUT).passedValidation());
        }

        @Test
        public void shouldRejectNullConnectionLogging() {
            conLog = null;
            assertFalse(validator.validate(conLog, PUT).passedValidation());
        }

        @Test
        public void shouldRejectNullConnectionLoggingEnabledValue() {
            conLog.setEnabled(null);
            assertFalse(validator.validate(conLog, PUT).passedValidation()); 
        }

//        @Test
//        public void shouldRejectNonBooleanEnabledValue() {
//            conLog.setEnabled("Crap");
//            assertFalse(validator.validate(conLog, PUT).passedValidation());
//        }
    }
}