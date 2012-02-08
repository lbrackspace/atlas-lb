package org.openstack.atlas.api.validation.validators;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;


@RunWith(Enclosed.class)
public class SslTerminationValidatorTest {

    public static class WhenValidatingPut {

        private SslTermination sslTermination;
        private SslTerminationValidator validator;

        @Before
        public void standUp() {
            validator = new SslTerminationValidator();

            sslTermination = new SslTermination();

        }

        @Test
        public void shouldFailForInvalidObject() {
            assertFalse(validator.validate(new SslTermination(), PUT).passedValidation());
        }

        @Test
        public void shouldFailIfOnlyICert() {
            sslTermination.setIntermediateCertificate("BLIGGITYBLAH");
            assertFalse(validator.validate(sslTermination, PUT).passedValidation());
        }

        @Test
        public void shouldFailIfOnlyCert() {
            sslTermination.setCertificate("BLIGGITYBLAH");
            sslTermination.setPrivatekey(null);
            assertFalse(validator.validate(sslTermination, PUT).passedValidation());
        }

        @Test
        public void shouldFailIfOnlyKey() {
            sslTermination.setPrivatekey("BLIGGITYBLAH");
            assertFalse(validator.validate(sslTermination, PUT).passedValidation());
        }

        @Test
        public void shouldFailIfOnlyKeyAndCert() {
            sslTermination.setPrivatekey("BLIGGITYBLAH");
            sslTermination.setCertificate("BLIGGITYBLAH");
            assertFalse(validator.validate(sslTermination, PUT).passedValidation());
        }

        @Test
        public void shouldAcceptValidSslTermination() {
            sslTermination.setPrivatekey("BLIGGITYBLAH");
            sslTermination.setCertificate("BLIGGITYBLAH");
            sslTermination.setSecurePort(443);
            assertTrue(validator.validate(sslTermination, PUT).passedValidation());
        }

        @Test
        public void shouldAcceptFullValidSslTermination() {
            sslTermination.setPrivatekey("BLIGGITYBLAH");
            sslTermination.setCertificate("BLIGGITYBLAH");
            sslTermination.setIntermediateCertificate("BLIGGITYBLAH");
            sslTermination.setSecurePort(443);
            sslTermination.setEnabled(true);
            sslTermination.setSecureTrafficOnly(true);
            assertTrue(validator.validate(sslTermination, PUT).passedValidation());
        }

        @Test
        public void shouldAcceptNonRequiredOnly() {
            sslTermination.setSecurePort(443);
            sslTermination.setEnabled(true);
            sslTermination.setSecureTrafficOnly(true);
            assertTrue(validator.validate(sslTermination, PUT).passedValidation());
        }

        @Test
        public void shouldFailIfCertOrKeyIsNull() {
            sslTermination.setSecurePort(443);
            sslTermination.setEnabled(true);
            sslTermination.setSecureTrafficOnly(true);
            assertTrue(validator.validate(sslTermination, PUT).passedValidation());
        }

        @Test
        public void shouldFailIfCertOrKeyIsNullAndICertIsNot() {
            sslTermination.setSecurePort(443);
            sslTermination.setEnabled(true);
            sslTermination.setSecureTrafficOnly(true);
            sslTermination.setIntermediateCertificate("blahICert");
            assertFalse(validator.validate(sslTermination, PUT).passedValidation());
        }

        @Test
        public void shouldAcceptSecurePortOnly() {
            sslTermination.setSecurePort(443);
            assertTrue(validator.validate(sslTermination, PUT).passedValidation());
        }

        @Test
        public void shouldAcceptEnabledOnly() {
            sslTermination.setEnabled(true);
            assertTrue(validator.validate(sslTermination, PUT).passedValidation());
        }

        @Test
        public void shouldAcceptEnabledisSecureTrafficOnly() {
            sslTermination.setEnabled(true);
            sslTermination.setSecureTrafficOnly(true);
            assertTrue(validator.validate(sslTermination, PUT).passedValidation());
        }

        @Test
        public void shouldAcceptisSecureTrafficOnly() {
            sslTermination.setSecureTrafficOnly(true);
            assertTrue(validator.validate(sslTermination, PUT).passedValidation());
        }
    }
}
