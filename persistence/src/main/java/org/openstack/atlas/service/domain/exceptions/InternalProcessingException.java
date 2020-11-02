package org.openstack.atlas.service.domain.exceptions;

public class InternalProcessingException extends Exception {

    public InternalProcessingException(String message) {
        super(message);
    }

    public InternalProcessingException(String message, Throwable th) {
        super(message, th);
    }

    public InternalProcessingException(Throwable th) {
        super(th);
    }

    public InternalProcessingException() {
        super();
    }
}
