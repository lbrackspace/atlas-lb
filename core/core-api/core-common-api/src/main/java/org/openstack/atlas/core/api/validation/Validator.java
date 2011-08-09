package org.openstack.atlas.core.api.validation;

import org.openstack.atlas.core.api.validation.exception.ValidationChainExecutionException;
import org.openstack.atlas.core.api.validation.result.ValidatorResult;

public interface Validator<T> {
    ValidatorResult validate(T objectToValidate, Object context) throws ValidationChainExecutionException;
}
