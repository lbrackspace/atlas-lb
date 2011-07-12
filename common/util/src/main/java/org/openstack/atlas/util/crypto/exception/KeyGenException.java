package org.openstack.atlas.util.crypto.exception;

/**
 * Thrown if an error occurs while decrypting data.
 */
public class KeyGenException extends Exception {

    private static final long serialVersionUID = 5381976961767478722L;

    public KeyGenException(String s) {
        super(s);
    }

    public KeyGenException(String s, Throwable cause) {
        super(s, cause);
    }

    public KeyGenException(Throwable cause) {
        super(cause);
    }
}
