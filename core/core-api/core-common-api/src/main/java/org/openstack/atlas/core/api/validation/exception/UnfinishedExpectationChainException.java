package org.openstack.atlas.core.api.validation.exception;

public class UnfinishedExpectationChainException extends ValidationException {

    public UnfinishedExpectationChainException(String string) {
        super(string);
    }
}
