package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Limit;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.RateLimit;
import org.openstack.atlas.api.validation.validators.ResourceValidator;
import org.openstack.atlas.api.validation.validators.ValidatorUtilities;
import org.openstack.atlas.api.validation.verifiers.MustBeNonNegativeInteger;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.verifiers.MustBeNonNegativeInteger;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;
import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class AccountLimitValidator implements ResourceValidator<Limit> {

    private final Validator<Limit> validator;

    public AccountLimitValidator() {
        validator = build(new ValidatorBuilder<Limit>(Limit.class) {
            {
                // POST EXPECTATIONS
                result(validationTarget().getName()).must().exist().forContext(POST).withMessage("Must provide a valid limit name.");
                result(validationTarget().getValue()).must().adhereTo(new MustBeNonNegativeInteger()).forContext(POST).withMessage("Must provide a valid limit value.");

                // PUT EXPECTATIONS
                //Who's gonna validate the name?
                result(validationTarget().getValue()).if_().exist().then().must().adhereTo(new MustBeNonNegativeInteger()).forContext(PUT).withMessage("Must provide a valid limit value.");
            }
        });
    }

    @Override
    public ValidatorResult validate(Limit rateLimit, Object httpRequestType) {
        ValidatorResult result = validator.validate(rateLimit, httpRequestType);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<Limit> getValidator() {
        return validator;
    }
}
