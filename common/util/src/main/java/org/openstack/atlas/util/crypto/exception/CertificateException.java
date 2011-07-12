package org.openstack.atlas.util.crypto.exception;

/**
 * Thrown if an error occurs while decrypting data.
 */
public class CertificateException extends Exception {

    private static final long serialVersionUID = -7882833455514647396L;

    public CertificateException(String s) {
        super(s);
    }

    public CertificateException(String s, Throwable cause) {
        super(s, cause);
    }

    public CertificateException(Throwable cause) {
        super(cause);
    }
}
