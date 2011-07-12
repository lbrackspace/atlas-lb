package org.openstack.atlas.util.crypto.exception;

/**
 * Thrown if an error occurs while decrypting data.
 */
public class ValidationException extends Exception {

    private static final long serialVersionUID = -8571806928046763514L;

    public ValidationException(String s) {
        super(s);
    }

    public ValidationException(String s, Throwable cause) {
        super(s, cause);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }
}
