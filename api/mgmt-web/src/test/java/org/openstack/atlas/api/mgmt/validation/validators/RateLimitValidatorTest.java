package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.RateLimit;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket;
import org.openstack.atlas.api.mgmt.validation.validators.RateLimitValidator;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.Calendar;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class RateLimitValidatorTest {

    public static class WhenValidatingPostRequest {
        private RateLimitValidator validator;
        private RateLimit rateLimit;
        private Ticket ticket;

        @Before
        public void standUp() {
            validator = new RateLimitValidator();
            rateLimit = new RateLimit();
            ticket = new Ticket();

            ticket.setTicketId("1234");
            ticket.setComment("My first comment!");

            rateLimit.setTicket(ticket);
            rateLimit.setExpirationTime(Calendar.getInstance());
            rateLimit.setMaxRequestsPerSecond(150);
        }

        @Test
        public void shouldAcceptValidRateLimitObject() {
            ValidatorResult result = validator.validate(rateLimit, POST);
            assertTrue(result.passedValidation());
        }

        @Test
        public void shouldRejectWhenMissingTicket() {
            rateLimit.setTicket(null);
            ValidatorResult result = validator.validate(rateLimit, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectWhenTicketIsEmpty() {
            rateLimit.getTicket().setTicketId("");
            rateLimit.getTicket().setComment("");
            ValidatorResult result = validator.validate(rateLimit, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectWhenMissingExpirationTime() {
            rateLimit.setExpirationTime(null);
            ValidatorResult result = validator.validate(rateLimit, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectWhenMissingMaxRequestsPerSecond() {
            rateLimit.setMaxRequestsPerSecond(null);
            ValidatorResult result = validator.validate(rateLimit, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectWhenMaxRequestPerSecondIsLessThanZero() {
            rateLimit.setMaxRequestsPerSecond(-1);
            ValidatorResult result = validator.validate(rateLimit, POST);
            assertFalse(result.passedValidation());
        }
    }

    public static class WhenValidatingPutRequest {
        private RateLimitValidator validator;
        private RateLimit rateLimit;

        @Before
        public void standUp() {
            validator = new RateLimitValidator();
            rateLimit = new RateLimit();

            rateLimit.setExpirationTime(Calendar.getInstance());
            rateLimit.setMaxRequestsPerSecond(150);
        }

        @Test
        public void shouldAcceptValidRateLimitObject() {
            ValidatorResult result = validator.validate(rateLimit, PUT);
            assertTrue(result.passedValidation());
        }

        @Test
        public void shouldRejectWhenTicketIsNotNull() {
            Ticket ticket = new Ticket();
            ticket.setTicketId("1234");
            ticket.setComment("My first comment!");
            rateLimit.setTicket(ticket);

            ValidatorResult result = validator.validate(rateLimit, PUT);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectWhenMissingAllAttributes() {
            ValidatorResult result = validator.validate(new RateLimit(), PUT);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectWhenMaxRequestPerSecondIsLessThanZero() {
            rateLimit.setMaxRequestsPerSecond(-1);
            ValidatorResult result = validator.validate(rateLimit, PUT);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldAcceptWhenMaxRequestPerSecondIsZero() {
            rateLimit.setMaxRequestsPerSecond(0);
            ValidatorResult result = validator.validate(rateLimit, PUT);
            assertTrue(result.passedValidation());
        }
    }
}
