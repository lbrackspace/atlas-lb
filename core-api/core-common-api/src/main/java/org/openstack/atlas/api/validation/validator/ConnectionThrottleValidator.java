package org.openstack.atlas.api.validation.validator;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.builder.ConnectionThrottleValidatorBuilder;
import org.openstack.atlas.core.api.v1.ConnectionThrottle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

@Component
@Scope("request")
public class ConnectionThrottleValidator implements ResourceValidator<ConnectionThrottle> {
    protected Validator<ConnectionThrottle> validator;
    protected ConnectionThrottleValidatorBuilder ruleBuilder;

    @Autowired
    public ConnectionThrottleValidator(ConnectionThrottleValidatorBuilder ruleBuilder) {
        this.ruleBuilder = ruleBuilder;
        validator = build(ruleBuilder);
    }

    @Override
    public ValidatorResult validate(ConnectionThrottle connectionLimits, Object requestType) {
        ValidatorResult result = validator.validate(connectionLimits, requestType);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<ConnectionThrottle> getValidator() {
        return validator;
    }
}
