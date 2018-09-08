package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.RateLimit;
import org.openstack.atlas.api.validation.validators.ResourceValidator;
import org.openstack.atlas.api.validation.validators.ValidatorUtilities;
import org.openstack.atlas.api.validation.verifiers.MustBeNonNegativeInteger;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.verifiers.MustBeNonNegativeInteger;
import org.openstack.atlas.api.validation.verifiers.MustNotBeEmptyOrNull;
import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;
import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class RateLimitValidator implements ResourceValidator<RateLimit> {

    private final Validator<RateLimit> validator;

    public RateLimitValidator() {
        validator = build(new ValidatorBuilder<RateLimit>(RateLimit.class) {
            {
                // POST EXPECTATIONS
                result(validationTarget().getTicket()).must().exist().forContext(POST).withMessage("Must provide a ticket for the rate limit.");
                result(validationTarget().getTicket()).must().delegateTo(new TicketValidator().getValidator(), POST).forContext(POST);
                result(validationTarget().getExpirationTime()).must().exist().forContext(POST).withMessage("Must provide an expiration date/time for the rate limit.");
                result(validationTarget().getMaxRequestsPerSecond()).must().exist().forContext(POST).withMessage("Must provide the maximum number of request per second for the rate limit.");

                // PUT EXPECTATIONS
                must().adhereTo(new Verifier<RateLimit>() {
                    @Override
                    public VerifierResult verify(RateLimit rateLimit) {
                        return new VerifierResult(rateLimit.getExpirationTime() != null || rateLimit.getMaxRequestsPerSecond() != null);
                    }
                }).forContext(PUT).withMessage("The rate limit must have at least one of the following to update: expirationTime, maxRequestsPerSecond.");
                result(validationTarget().getTicket()).must().not().exist().forContext(PUT).withMessage("Ticket element cannot be updated here. Please remove.");

                // SHARED EXPECTATIONS
                result(validationTarget().getMaxRequestsPerSecond()).if_().exist().then().must().adhereTo(new MustBeNonNegativeInteger()).withMessage("The maximum number of request per second for the rate limit must be a positive integer.");
                result(validationTarget().getMaxRequestsPerSecond()).if_().exist().then().must().adhereTo(new MustNotBeEmptyOrNull()).withMessage("Max requests per second much contain a value.");
            }
        });
    }

    @Override
    public ValidatorResult validate(RateLimit rateLimit, Object httpRequestType) {
        ValidatorResult result = validator.validate(rateLimit, httpRequestType);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<RateLimit> getValidator() {
        return validator;
    }
}
