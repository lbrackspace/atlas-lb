package org.openstack.atlas.service.domain.exception;

public class AccountMismatchException extends PersistenceServiceException {
    private String message;

    public AccountMismatchException(final String message) {
        this.message = message;
    }

    public AccountMismatchException(String message, Throwable th) {
        super(message, th);
    }

    public AccountMismatchException(Throwable th) {
        super(th);
    }

    @Override
    public String getMessage() {
        return message;
    }
}
