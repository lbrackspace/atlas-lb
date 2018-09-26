package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket;
import org.openstack.atlas.api.mgmt.validation.validators.TicketValidator;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.openstack.atlas.api.helpers.ResultMessage.resultMessage;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class TicketValidatorTest {

    public static class WhenValidatingPost {
        private TicketValidator validator;
        private Ticket ticket;

        @Before
        public void standUp() {
            validator = new TicketValidator();

            ticket = new Ticket();
            ticket.setTicketId("1234");
            ticket.setComment("My first ticket! Yuppee!");
        }

        @Test
        public void shouldAcceptValidTicket() {
            ValidatorResult result = validator.validate(ticket, POST);
            assertTrue(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectWhenMissingTicketId() {
            ticket.setTicketId(null);

            ValidatorResult result = validator.validate(ticket, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectWhenTicketIdIsEmpty() {
            ticket.setTicketId("");

            ValidatorResult result = validator.validate(ticket, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectWhenMissingComment() {
            ticket.setComment(null);

            ValidatorResult result = validator.validate(ticket, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectWhenCommentIsEmpty() {
            ticket.setComment("");

            ValidatorResult result = validator.validate(ticket, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectWhenIdIsSet() {
            ticket.setId(1234);

            ValidatorResult result = validator.validate(ticket, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }
    }
}
