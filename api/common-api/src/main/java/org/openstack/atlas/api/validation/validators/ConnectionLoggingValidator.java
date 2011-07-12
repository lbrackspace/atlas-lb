package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionLogging;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class ConnectionLoggingValidator implements ResourceValidator<ConnectionLogging> {
    private Validator<ConnectionLogging> validator;
    private final Boolean bool = true;

    public ConnectionLoggingValidator() {
        validator = build(new ValidatorBuilder<ConnectionLogging>(ConnectionLogging.class) {
            {

                result(validationTarget().isEnabled()).must().exist().withMessage("Must specify whether connection logging is enabled or not.");
//                result(validationTarget().isEnabled()).must().adhereTo(new Verifier() {
//                    @Override
//                    public VerifierResult verify(Object obj) {
//                        if (obj instanceof Boolean){
//                            return new VerifierResult(true);
//                        }
//                        return new VerifierResult(false);
//                    }
//                }).withMessage("Must specify true or false");
            }
        });
    }

    @Override
    public ValidatorResult validate(ConnectionLogging conLog, Object context) {
        ValidatorResult result = validator.validate(conLog, context);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<ConnectionLogging> getValidator() {
        return validator;
    }
}