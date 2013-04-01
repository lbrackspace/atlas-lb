package org.openstack.atlas.restclients.atomhopper.exception;

public class NoAuthTokenException extends Exception {

    public NoAuthTokenException(String s) {
        super(s);
    }

    public NoAuthTokenException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
