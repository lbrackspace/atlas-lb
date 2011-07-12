package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.v1.PersistenceType;
import org.openstack.atlas.docs.loadbalancers.api.v1.SessionPersistence;
import org.openstack.atlas.api.validation.verifiers.MustBeInArray;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.verifiers.MustBeInArray;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class SessionPersistenceValidator implements ResourceValidator<SessionPersistence> {
    private final Validator<SessionPersistence> validator;

    public SessionPersistenceValidator() {
        validator = build(new ValidatorBuilder<SessionPersistence>(SessionPersistence.class) {
            {
                // SHARED EXPECTATIONS
                result(validationTarget().getPersistenceType()).must().exist().withMessage("Must provide a persistence type.");
                result(validationTarget().getPersistenceType()).if_().exist().then().must().adhereTo(new MustBeInArray(PersistenceType.values())).withMessage("Persistence type is invalid. Please specify a valid persistence type.");
            }
        });
    }

    @Override
    public ValidatorResult validate(SessionPersistence persistence, Object httpRequestType) {
        ValidatorResult result = validator.validate(persistence, httpRequestType);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<SessionPersistence> getValidator() {
        return validator;
    }
}
