package org.openstack.atlas.service.domain.exception;

import org.openstack.atlas.service.domain.common.ErrorMessages;

public class EntityExistsException extends PersistenceServiceException {

    private String message;

    public EntityExistsException(final String message) {
        this.message = message;
    }

    public EntityExistsException(String message, Throwable th) {
        super(message, th);
    }

    public EntityExistsException(Throwable th) {
        super(th);
    }

    public EntityExistsException(ErrorMessages messages) {
        this.message = messages.toString();
    }

    @Override
    public String getMessage() {
        return message;
    }
}
