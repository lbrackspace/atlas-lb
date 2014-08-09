package org.openstack.atlas.service.domain.exceptions;

public class PrivateKeyCryptException extends RuntimeException{

    public PrivateKeyCryptException(Throwable cause) {
        super(cause);
    }

    public PrivateKeyCryptException(String message, Throwable cause) {
        super(message, cause);
    }

    public PrivateKeyCryptException(String message) {
        super(message);
    }

    public PrivateKeyCryptException() {
    }


}
