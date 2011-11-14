package org.openstack.atlas.rax.api.validation.validator;

import org.openstack.atlas.api.v1.extensions.rax.ConnectionLogging;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.ResourceValidator;
import org.openstack.atlas.api.validation.validator.ValidatorUtilities;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

@Primary
@Component
@Scope("request")
public class RaxConnectionLoggingValidator implements ResourceValidator<ConnectionLogging> {

    private final Validator<ConnectionLogging> validator;

    public RaxConnectionLoggingValidator() {
        validator = build(new ValidatorBuilder<ConnectionLogging>(ConnectionLogging.class) {
            {
                // PUT EXPECTATIONS
                result(validationTarget().isEnabled()).must().exist().withMessage("Must specify whether connection logging is enabled or not.");
            }
        });
    }

    @Override
    public ValidatorResult validate(ConnectionLogging connectionLogging, Object httpRequestType) {
        ValidatorResult result = validator.validate(connectionLogging, httpRequestType);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<ConnectionLogging> getValidator() {
        return validator;
    }
}
