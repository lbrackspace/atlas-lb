package org.openstack.atlas.service.domain.exception;

public class UnprocessableEntityException extends PersistenceServiceException {
    private final String message;

    public UnprocessableEntityException(final String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
