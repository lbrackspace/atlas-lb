package org.openstack.atlas.api.validation.exceptions;

/**
 *
 * @author zinic
 */
public class UnfinishedExpectationChainException extends ValidationException {

    public UnfinishedExpectationChainException(String string) {
        super(string);
    }
}
