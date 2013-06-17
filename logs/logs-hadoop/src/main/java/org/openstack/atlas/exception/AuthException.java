package org.openstack.atlas.exception;

public class AuthException extends Exception {

    public AuthException() {
        super();
    }

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public AuthException(Throwable throwable) {
        super(throwable);
    }
}
