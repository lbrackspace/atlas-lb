package org.openstack.atlas.core.api.validation.validator;

import org.openstack.atlas.core.api.validation.Validator;
import org.openstack.atlas.core.api.validation.result.ValidatorResult;

public interface ResourceValidator<T> {
    public ValidatorResult validate(T objectToValidate, Object context);

    public Validator<T> getValidator();
}
