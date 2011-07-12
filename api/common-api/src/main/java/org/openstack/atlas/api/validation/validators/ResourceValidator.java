package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.results.ValidatorResult;

public interface ResourceValidator<T> {
    public ValidatorResult validate(T objectToValidate, Object context);

    public Validator<T> getValidator();
}
