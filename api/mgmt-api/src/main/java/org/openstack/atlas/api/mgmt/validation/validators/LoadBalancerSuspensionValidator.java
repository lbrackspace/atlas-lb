package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Suspension;
import org.openstack.atlas.api.validation.validators.ResourceValidator;
import org.openstack.atlas.api.validation.validators.ValidatorUtilities;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class LoadBalancerSuspensionValidator implements ResourceValidator<Suspension> {

    private final Validator<Suspension> validator;

    public LoadBalancerSuspensionValidator() {
        validator = build(new ValidatorBuilder<Suspension>(Suspension.class) {
            {
                // POST EXPECTATIONS
                result(validationTarget().getReason()).must().exist().forContext(POST).withMessage("Must provide a reason for suspension.");
                result(validationTarget().getTicket()).must().exist().forContext(POST).withMessage("Must provide a ticket.");
                result(validationTarget().getTicket()).must().delegateTo(new TicketValidator().getValidator(), POST).forContext(POST);
                result(validationTarget().getUser()).must().exist().forContext(POST).withMessage("Must provide the user name.");
            }
        });
    }

    @Override
    public ValidatorResult validate(Suspension backup, Object httpRequestType) {
        ValidatorResult result = validator.validate(backup, httpRequestType);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<Suspension> getValidator() {
        return validator;
    }

}
