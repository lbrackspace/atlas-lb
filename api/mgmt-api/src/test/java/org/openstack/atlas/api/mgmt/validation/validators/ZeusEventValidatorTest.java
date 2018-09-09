package org.openstack.atlas.api.mgmt.validation.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ZeusEvent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openstack.atlas.api.helpers.ResultMessage.resultMessage;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;

@RunWith(Enclosed.class)
public class ZeusEventValidatorTest {

    public static class WhenValidatingPost {
        private ZeusEventValidator validator;
        private ZeusEvent zeusEvent;

        @Before
        public void standUp() {
            validator = new ZeusEventValidator();

            zeusEvent = new ZeusEvent();
            zeusEvent.setCallbackHost("callbackHost");
            zeusEvent.setParamLine("some parameter line");
        }

        @Test
        public void shouldAcceptValidZeusEvent() {
            ValidatorResult result = validator.validate(zeusEvent, POST);
            assertTrue(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectWhenMissingCallbackHost() {
            zeusEvent.setCallbackHost(null);

            ValidatorResult result = validator.validate(zeusEvent, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectWhenMissingParamLine() {
            zeusEvent.setParamLine(null);

            ValidatorResult result = validator.validate(zeusEvent, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }
    }
}
