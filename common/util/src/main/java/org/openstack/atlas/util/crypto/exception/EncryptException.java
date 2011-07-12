package org.openstack.atlas.util.crypto.exception;

/**
 * Thrown if an error occurs while encrypting data.
 */
public class EncryptException extends Exception {

    private static final long serialVersionUID = -2193468517560342487L;

    public EncryptException(String s) {
        super(s);
    }

    public EncryptException(String s, Throwable cause) {
        super(s, cause);
    }

    public EncryptException(Throwable cause) {
        super(cause);
    }
}
