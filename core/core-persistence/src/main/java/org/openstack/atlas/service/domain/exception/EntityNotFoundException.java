package org.openstack.atlas.service.domain.exception;

public class EntityNotFoundException extends PersistenceServiceException {
    private String message;

    public EntityNotFoundException(final String message) {
        this.message = message;
    }

    public EntityNotFoundException(String message, Throwable th) {
        super(message, th);
    }

    public EntityNotFoundException(Throwable th) {
        super(th);
    }

    @Override
    public String getMessage() {
        return message;
    }
}
