package org.openstack.atlas.service.domain.exception;

public class ImmutableEntityException extends PersistenceServiceException {
    private final String message;

    public ImmutableEntityException(final String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
