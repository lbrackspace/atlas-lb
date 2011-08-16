package org.openstack.atlas.service.domain.exception;

public class BadRequestException extends PersistenceServiceException {

    private String message;

    public BadRequestException(final String message) {
        this.message = message;
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

    @Override
    public String getMessage() {
        return message;
    }
}
