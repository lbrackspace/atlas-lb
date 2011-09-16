package org.openstack.atlas.service.domain.exception;

import org.openstack.atlas.service.domain.common.ErrorMessages;

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

    public BadRequestException(ErrorMessages messages) {
        this.message = messages.toString();
    }

    @Override
    public String getMessage() {
        return message;
    }
}
