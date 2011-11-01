package org.openstack.atlas.api.validation.validator;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.result.ValidatorResult;

public interface ResourceValidator<T> {
    public ValidatorResult validate(T objectToValidate, Object context);

    public Validator<T> getValidator();
}
