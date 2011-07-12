package org.openstack.atlas.api.validation.exceptions;

/**
 *
 * @author John Hopper
 */
public class ValidationChainExecutionException extends ValidationException {

    public ValidationChainExecutionException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }
}
