package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket;
import org.openstack.atlas.api.validation.validators.ResourceValidator;
import org.openstack.atlas.api.validation.validators.ValidatorUtilities;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.ValidatorBuilder.build;


public class TicketValidator implements ResourceValidator<Ticket> {
    private final Validator<Ticket> validator;

    public TicketValidator() {
        validator = build(new ValidatorBuilder<Ticket>(Ticket.class) {
            {
                // SHARED EXPECTATIONS
                result(validationTarget().getId()).must().not().exist().withMessage("Ticket id field cannot be modified.");

                // POST EXPECTATIONS
                result(validationTarget().getTicketId()).must().exist().forContext(POST).withMessage("Must provide a ticketId for the ticket.");
                result(validationTarget().getTicketId()).must().not().beEmptyOrNull().forContext(POST).withMessage("Ticket id must not be empty.");
                result(validationTarget().getComment()).must().exist().forContext(POST).withMessage("Must provide a comment for the ticket.");
                result(validationTarget().getComment()).must().not().beEmptyOrNull().forContext(POST).withMessage("Ticket comment must not be empty.");
            }
        });
    }

    @Override
    public ValidatorResult validate(Ticket ticket, Object requestType) {
        ValidatorResult result = validator.validate(ticket, requestType);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<Ticket> getValidator() {
        return validator;
    }
}
