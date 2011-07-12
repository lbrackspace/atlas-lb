package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Suspension;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket;
import org.openstack.atlas.api.mgmt.validation.validators.LoadBalancerSuspensionValidator;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.junit.Before;
import org.junit.Test;

import static org.openstack.atlas.api.helpers.ResultMessage.resultMessage;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LoadBalancerSuspensionValidatorTest {
    private LoadBalancerSuspensionValidator lbsvalidator;
    private Suspension suspension;
    private Ticket ticket;

    @Before
    public void standUp() {
        lbsvalidator = new LoadBalancerSuspensionValidator();
        suspension = new Suspension();
        ticket = new Ticket();

        ticket.setTicketId("1234");
        ticket.setComment("My first ticket! Yuppee!");

        suspension.setReason("repo");
        suspension.setTicket(ticket);
        suspension.setUser("bob");
    }

    @Test
    public void shouldAcceptValidLbS() {
        ValidatorResult result = lbsvalidator.validate(suspension, HttpRequestType.POST);
        assertTrue(result.passedValidation());
    }

    @Test
    public void shouldRejectUser() {
        suspension.setUser("bob");
        ValidatorResult result = lbsvalidator.validate(suspension, HttpRequestType.POST);
        assertTrue(result.passedValidation());
    }

    @Test
    public void shouldRejectNullReason() {
        suspension.setReason(null);
        ValidatorResult result = lbsvalidator.validate(suspension, POST);
        assertFalse(resultMessage(result, POST), result.passedValidation());
    }

    @Test
    public void shouldRejectNullTicket() {
        suspension.setTicket(null);
        ValidatorResult result = lbsvalidator.validate(suspension, POST);
        assertFalse(resultMessage(result, POST), result.passedValidation());
    }

    @Test
    public void shouldRejectWhenTicketIsEmpty() {
        suspension.getTicket().setTicketId("");
        suspension.getTicket().setComment("");
        ValidatorResult result = lbsvalidator.validate(suspension, POST);
        assertFalse(resultMessage(result, POST), result.passedValidation());
    }
}
