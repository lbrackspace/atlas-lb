package org.openstack.atlas.api.validation.exceptions;

/**
 *
 * @author John Hopper
 */
public class ReflectedValidationException extends ValidationException {

    public ReflectedValidationException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }
}
