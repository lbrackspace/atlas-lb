package org.openstack.atlas.service.domain.exception;

public class LimitReachedException extends PersistenceServiceException {

    private String message;

    public LimitReachedException(final String message) {
        this.message = message;
    }

    public LimitReachedException(String message, Throwable th) {
        super(message, th);
    }

    public LimitReachedException(Throwable th) {
        super(th);
    }

    public LimitReachedException() {
        super();
    }

    @Override
    public String getMessage() {
        return message;
    }
}
