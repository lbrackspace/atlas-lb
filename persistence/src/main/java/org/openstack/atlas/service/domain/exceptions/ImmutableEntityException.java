package org.openstack.atlas.service.domain.exceptions;

public class ImmutableEntityException extends Exception {

    public ImmutableEntityException() {
        super();
    }

    public ImmutableEntityException(String message) {
        super(message);
    }

    public ImmutableEntityException(String message, Throwable th) {
        super(message, th);
    }

    public ImmutableEntityException(Throwable th) {
        super(th);
    }
}
