package org.openstack.atlas.util.ca.exceptions;

public class NoSuchAlgorithmException extends RsaException {

    public NoSuchAlgorithmException() {
    }

    public NoSuchAlgorithmException(String msg) {
        super(msg);
    }

    public NoSuchAlgorithmException(Throwable th) {
        super(th);
    }

    public NoSuchAlgorithmException(String msg, Throwable th) {
        super(msg, th);
    }
}
