package org.openstack.atlas.service.domain.exception;

import org.openstack.atlas.service.domain.common.ErrorMessages;

public class OutOfVipsException extends PersistenceServiceException {
    private final String message;

    public OutOfVipsException(final String message) {
        this.message = message;
    }

    public OutOfVipsException(ErrorMessages messages) {
        this.message = messages.toString();
    }

    @Override
    public String getMessage() {
        return message;
    }
}
