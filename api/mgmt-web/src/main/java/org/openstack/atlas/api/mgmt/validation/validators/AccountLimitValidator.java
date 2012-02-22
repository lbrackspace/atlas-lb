package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.validators.ResourceValidator;
import org.openstack.atlas.api.validation.validators.ValidatorUtilities;
import org.openstack.atlas.api.validation.verifiers.MustBeNonNegativeInteger;
import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Limit;
import org.openstack.atlas.service.domain.entities.AccountLimitType;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;

public class AccountLimitValidator implements ResourceValidator<Limit> {

    private final Validator<Limit> validator;

    public AccountLimitValidator() {
        validator = build(new ValidatorBuilder<Limit>(Limit.class) {
            {
                // POST EXPECTATIONS
                result(validationTarget().getName()).must().exist().forContext(POST).withMessage("Must provide a valid limit name.");
                result(validationTarget().getName()).must().adhereTo(new Verifier<String>() {
                    @Override
                    public VerifierResult verify(String obj) {
                        VerifierResult result = new VerifierResult(false);
                        for (AccountLimitType type : AccountLimitType.values()) {
                            try {
                                result = new VerifierResult(type.equals(AccountLimitType.valueOf(obj)));
                            } catch (Exception e) {
                                return result;
                            }
                            if (result.passed()) {
                                break;
                            }
                        }
                        return result;
                    }

                }).forContext(POST).withMessage("Must provide a valid limit name.");
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
