package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Tickets;
import org.openstack.atlas.api.validation.validators.ResourceValidator;
import org.openstack.atlas.api.validation.validators.ValidatorUtilities;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.ValidatorBuilder.build;


public class TicketsValidator implements ResourceValidator<Tickets> {
    private final Validator<Tickets> validator;

    public TicketsValidator() {
        validator = build(new ValidatorBuilder<Tickets>(Tickets.class) {
            {
                // SHARED EXPECTATIONS
                result(validationTarget().getTickets()).must().exist().withMessage("Must provide a ticket.");
                //POST EXPECTATATIONS
                result(validationTarget().getTickets()).if_().exist().then().must().haveSizeOfAtLeast(1).forContext(POST).withMessage("Must provide at least one ticket.");
                result(validationTarget().getTickets()).must().delegateTo(new TicketValidator().getValidator(), POST).forContext(POST);
            }
        });
    }

    @Override
    public ValidatorResult validate(Tickets tickets, Object requestType) {
        ValidatorResult result = validator.validate(tickets, requestType);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<Tickets> getValidator() {
        return validator;
    }
}
