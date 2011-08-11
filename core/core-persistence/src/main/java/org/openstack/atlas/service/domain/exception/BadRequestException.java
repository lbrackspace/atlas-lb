package org.openstack.atlas.service.domain.exception;

public class BadRequestException extends Exception {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable th) {
        super(message, th);
    }

    public BadRequestException(Throwable th) {
        super(th);
    }

    public BadRequestException() {
        super();
    }
}
