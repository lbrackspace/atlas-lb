package org.rackspace.capman.tools.ca.exceptions;

public class NotAnRSAKeyException extends RsaException {

    public NotAnRSAKeyException() {
    }

    public NotAnRSAKeyException(String msg) {
        super(msg);
    }

    public NotAnRSAKeyException(Throwable th) {
        super(th);
    }

    public NotAnRSAKeyException(String msg, Throwable th) {
        super(msg, th);
    }
}
