package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Tickets;
import org.openstack.atlas.api.mgmt.validation.validators.TicketsValidator;
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
public class TicketsValidatorTest {

    public static class WhenValidatingPost {
        private TicketsValidator validator;
        private Tickets tickets;
        private Ticket ticket;

        @Before
        public void standUp() {
            validator = new TicketsValidator();

            ticket = new Ticket();
            ticket.setTicketId("1234");
            ticket.setComment("My first ticket! Yuppee!");

            tickets = new Tickets();
            tickets.getTickets().add(ticket);
        }

        @Test
        public void shouldAcceptValidTicket() {
            ValidatorResult result = validator.validate(tickets, POST);
            assertTrue(resultMessage(result, POST), result.passedValidation());
        }

        @Test
        public void shouldRejectWhenThereIsNoTicket() {
            tickets.getTickets().remove(ticket);

            ValidatorResult result = validator.validate(tickets, POST);
            assertFalse(resultMessage(result, POST), result.passedValidation());
        }
    }
}
