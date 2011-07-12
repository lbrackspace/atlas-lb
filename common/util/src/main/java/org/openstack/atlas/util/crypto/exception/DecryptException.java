package org.openstack.atlas.util.crypto.exception;

/**
 * Thrown if an error occurs while decrypting data.
 */
public class DecryptException extends Exception {

    private static final long serialVersionUID = -1109474403096949089L;

    public DecryptException(String s) {
        super(s);
    }

    public DecryptException(String s, Throwable cause) {
        super(s, cause);
    }

    public DecryptException(Throwable cause) {
        super(cause);
    }
}
