package org.openstack.atlas.adapter.exceptions;

public class InsufficientRequestException extends Exception {

    private static final long serialVersionUID = -1850300663038738768L;

    public InsufficientRequestException(String message) {
        super(message);
    }

    public InsufficientRequestException(String msg, Throwable th) {
        super(msg, th);
    }

    public InsufficientRequestException(Throwable th) {
        super(th);
    }

    public InsufficientRequestException() {
        super();
    }
}
