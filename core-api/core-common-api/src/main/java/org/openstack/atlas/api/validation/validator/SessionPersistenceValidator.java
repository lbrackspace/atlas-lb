package org.openstack.atlas.api.validation.validator;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.builder.SessionPersistenceValidatorBuilder;
import org.openstack.atlas.core.api.v1.SessionPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

@Component
@Scope("request")
public class SessionPersistenceValidator implements ResourceValidator<SessionPersistence> {
    protected Validator<SessionPersistence> validator;
    protected SessionPersistenceValidatorBuilder ruleBuilder;

    @Autowired
    public SessionPersistenceValidator(SessionPersistenceValidatorBuilder ruleBuilder) {
        this.ruleBuilder = ruleBuilder;
        validator = build(ruleBuilder);
    }

    @Override
    public ValidatorResult validate(SessionPersistence sessionPersistence, Object context) {
        ValidatorResult result = validator.validate(sessionPersistence, context);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<SessionPersistence> getValidator() {
        return validator;
    }
}
