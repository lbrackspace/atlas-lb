package org.openstack.atlas.service.domain.exceptions;

public class VipCredentialsInvalidException extends Exception {

    public VipCredentialsInvalidException(String message) {
        super(message);
    }

    public VipCredentialsInvalidException(String message, Throwable th) {
        super(message, th);
    }

    public VipCredentialsInvalidException(Throwable th) {
        super(th);
    }

    public VipCredentialsInvalidException() {
        super();
    }
}
