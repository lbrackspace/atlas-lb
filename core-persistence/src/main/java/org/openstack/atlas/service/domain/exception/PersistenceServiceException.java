package org.openstack.atlas.service.domain.exception;

public class PersistenceServiceException extends Exception {

    public PersistenceServiceException() {
        super();
    }

    public PersistenceServiceException(Exception e) {
        super(e);
    }

    public PersistenceServiceException(String message, Throwable th) {
        super(message, th);
    }

    public PersistenceServiceException(Throwable th) {
        super(th);
    }

}