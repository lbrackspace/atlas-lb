package org.openstack.atlas.service.domain.exception;

public class OutOfVipsException extends PersistenceServiceException {
    private final String message;

    public OutOfVipsException(final String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
