package org.openstack.atlas.api.validation;

import org.openstack.atlas.api.validation.exceptions.ValidationChainExecutionException;
import org.openstack.atlas.api.validation.results.ValidatorResult;

public interface Validator<T> {
    ValidatorResult validate(T objectToValidate, Object context) throws ValidationChainExecutionException;
}
