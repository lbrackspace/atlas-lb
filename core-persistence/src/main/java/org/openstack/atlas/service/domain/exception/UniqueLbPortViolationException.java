package org.openstack.atlas.service.domain.exception;

public class UniqueLbPortViolationException extends PersistenceServiceException {
    private final String message;

    public UniqueLbPortViolationException(final String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
