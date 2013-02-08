/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitor;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitorType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitorType.CONNECT;
import static org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitorType.HTTP;
import static org.openstack.atlas.api.helpers.ResultMessage.resultMessage;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class HealthMonitorValidatorTest {

    public static class whenValidatingPut {

        private final int MAXSTR = 128;
        private HealthMonitorValidator hmv;
        private HealthMonitor hm;
        private ValidatorResult result;
        private final String bigStr = getBigStr(MAXSTR);

        public static Integer intornull(String int_str) {
            return (int_str == null) ? null : Integer.parseInt(int_str);
        }

        public static HealthMonitor initHealthMonitor(String i, HealthMonitorType type, String d, String to, String abd, String p, String sr, String br) {
            HealthMonitor nhm = new HealthMonitor();
            nhm.setId(intornull(i));
            nhm.setType(type);
            nhm.setDelay(intornull(d));
            nhm.setTimeout(intornull(to));
            nhm.setAttemptsBeforeDeactivation(intornull(abd));
            nhm.setPath(p);
            nhm.setStatusRegex(sr);
            nhm.setBodyRegex(br);
            return nhm;
        }

        private String getBigStr(int size) {
            int i;
            StringBuilder sb = new StringBuilder();
            for (i = 0; i <= size; i++) {
                sb.append("X");
            }
            return sb.toString();
        }

        @Before
        public void setup() {
            int i;
            hmv = new HealthMonitorValidator();
            hm = new HealthMonitor();
        }

        @Test
        public void shouldRejectInvalidStatusRegex() {
            hm = initHealthMonitor(null, HTTP, "10", null, null, "/", "^*****[234][0-9][0-9]$", null);
            result = hmv.validate(hm, PUT);
            assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldAcceptValidStatusRegex() {
            hm = initHealthMonitor(null, HTTP, "10", null, null, "/", "^[234][0-9][0-9]$", null);
            result = hmv.validate(hm, PUT);
            assertTrue(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldRejectInvalidBodyRegex() {
            hm = initHealthMonitor(null, HTTP, "10", null, null, "/", null, "^*****[234][0-9][0-9]$");
            result = hmv.validate(hm, PUT);
            assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldAcceptValidBodyRegex() {
            hm = initHealthMonitor(null, HTTP, "10", null, null, "/", null, "^[234][0-9][0-9]$");
            result = hmv.validate(hm, PUT);
            assertTrue(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldAcceptValidPath() {
            hm = initHealthMonitor(null, HTTP, "10", null, null, "/", null, null);
            result = hmv.validate(hm, PUT);
            assertTrue(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldAcceptValidPathWithLongPath() {
            hm = initHealthMonitor(null, HTTP, "10", null, null, "/fail/this/should/not/fail", null, null);
            result = hmv.validate(hm, PUT);
            assertTrue(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldRejectInalidPath() {
            hm = initHealthMonitor(null, HTTP, "10", null, null, "FAIL", null, null);
            result = hmv.validate(hm, PUT);
            assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldAcceptValidPathPost() {
            hm = initHealthMonitor(null, HTTP, "10", null, null, "/", null, null);
            result = hmv.validate(hm, PUT);
            assertTrue(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldAcceptValidPathWithLongPathPost() {
            hm = initHealthMonitor(null, HTTP, "10", null, null, "/fail/this/should/not/fail", null, null);
            result = hmv.validate(hm, PUT);
            assertTrue(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectInalidPathPost() {
            hm = initHealthMonitor(null, HTTP, "10", null, null, "FAIL", null, null);
            result = hmv.validate(hm, PUT);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void ShouldAcceptValidPutConnectDelay() {
            hm = initHealthMonitor(null, CONNECT, "10", null, null, null, null, null);
            result = hmv.validate(hm, PUT);
            assertTrue(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void ShouldAcceptValidConnectPutTimeout() {
            hm = initHealthMonitor(null, CONNECT, null, "12", null, null, null, null);
            result = hmv.validate(hm, PUT);
            assertTrue(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void ShouldAcceptValidConnectPutABD() {
            hm = initHealthMonitor(null, CONNECT, null, null, "10", null, null, null);
            result = hmv.validate(hm, PUT);
            assertTrue(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void ShouldRejectConnectionPutWithNoConnectionAttributes() {
            hm = initHealthMonitor(null, CONNECT, null, null, null, null, null, null);
            result = hmv.validate(hm, PUT);
            assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldRejectConnectionPutwithOutRegEx() {
            hm = initHealthMonitor(null, CONNECT, "10", "20", "10", "/mnt/pfft", null, null);
            result = hmv.validate(hm, PUT);
            assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldRejectConnectionPutwithoutPathOrBodyRegEx() {
            hm = initHealthMonitor(null, CONNECT, "10", "20", "10", null, ".*", null);
            result = hmv.validate(hm, PUT);
            assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldRejectConnectionPutwithoutBodyRegEx() {
            hm = initHealthMonitor(null, CONNECT, "10", "20", "10", "/mnt/pfft", null, ".*");
            result = hmv.validate(hm, PUT);
            assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldAcceptValidHTTP() {
            hm = initHealthMonitor(null, HTTP, "10", "20", "10", "/mnt/pfft", ".*", ".*");
            result = hmv.validate(hm, PUT);
            assertTrue(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldRejectBlankType() {
            hm = initHealthMonitor(null, null, "10", "20", "10", "/mnt/pfft", ".*", ".*");
            result = hmv.validate(hm, PUT);
            assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldAcceptValidPutHttpDelay() {
            hm = initHealthMonitor(null, HTTP, "10", null, null, null, null, null);
            result = hmv.validate(hm, PUT);
            assertTrue(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldAcceptValidPutHttpTimeout() {
            hm = initHealthMonitor(null, HTTP, null, "30", null, null, null, null);
            result = hmv.validate(hm, PUT);
            assertTrue(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldAcceptValidPutHttpABD() {
            hm = initHealthMonitor(null, HTTP, null, null, "10", null, null, null);
            result = hmv.validate(hm, PUT);
            assertTrue(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldAcceptValidPutHttpPath() {
            hm = initHealthMonitor(null, HTTP, null, null, null, "/mnt/haha", null, null);
            result = hmv.validate(hm, PUT);
            assertTrue(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldAcceptValidPutHttpStatusRegex() {
            hm = initHealthMonitor(null, HTTP, null, null, null, null, ".*", ".*");
            result = hmv.validate(hm, PUT);
            assertTrue(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldAcceptValidPutHttpBodyRegex() {
            hm = initHealthMonitor(null, HTTP, null, null, null, null, null, ".*");
            result = hmv.validate(hm, PUT);
            assertTrue(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldRejectPutWithNoHttpAttributes() {
            hm = initHealthMonitor(null, HTTP, null, null, null, null, null, null);
            result = hmv.validate(hm, PUT);
            assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldRejectPutConnesWithOutType() {
            hm = initHealthMonitor(null, null, "10", "20", "10", null, null, null);
            result = hmv.validate(hm, PUT);
            assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldRejectPutHttpssWithOutType() {
            hm = initHealthMonitor(null, null, "10", "20", "10", "/mnt/pfft", ".*", ".*");
            result = hmv.validate(hm, PUT);
            assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldRejectInvalidDelayRangeMin() {
            hm.setDelay(0);
            result = hmv.validate(hm, PUT);
            assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldRejectInvalidDelayRangeMax() {
            hm.setDelay(3601);
            result = hmv.validate(hm, PUT);
            assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldRejectInvalidTimeoutRangeMin() {
            hm.setDelay(0);
            result = hmv.validate(hm, PUT);
            assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldRejectInvalidTimeoutRangeMax() {
            hm.setDelay(301);
            result = hmv.validate(hm, PUT);
            assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldRejectInvalidAttemptsBeforeDeactivationRangeMin() {
            hm.setDelay(0);
            result = hmv.validate(hm, PUT);
            assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldRejectInvalidAttemptsBeforeDeactivationRangeMax() {
            hm.setDelay(11);
            result = hmv.validate(hm, PUT);
            assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldRejectPathBiggerthen128Chars() {
            hm = initHealthMonitor(null, HTTP, "10", "20", "10", bigStr, ".*", ".*");
            result = hmv.validate(hm, PUT);
            assertFalse(resultMessage(result, PUT), result.passedValidation());
        }

        @Test
        public void shouldRejectBodyRegExBiggerthen128Chars() {
            hm = initHealthMonitor(null, HTTP, "10", "20", "10", "/mnt/pfft", bigStr, ".*");
            result = hmv.validate(hm, PUT);
            assertFalse(resultMessage(result, PUT), result.passedValidation());
        }
    }
}