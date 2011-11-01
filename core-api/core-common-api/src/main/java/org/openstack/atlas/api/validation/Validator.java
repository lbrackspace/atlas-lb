package org.openstack.atlas.api.validation;

import org.openstack.atlas.api.validation.exception.ValidationChainExecutionException;
import org.openstack.atlas.api.validation.result.ValidatorResult;

public interface Validator<T> {
    ValidatorResult validate(T objectToValidate, Object context) throws ValidationChainExecutionException;
}
