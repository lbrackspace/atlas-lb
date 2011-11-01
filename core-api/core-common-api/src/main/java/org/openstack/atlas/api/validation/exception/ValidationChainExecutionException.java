package org.openstack.atlas.api.validation.exception;

public class ValidationChainExecutionException extends ValidationException {

    public ValidationChainExecutionException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }
}
