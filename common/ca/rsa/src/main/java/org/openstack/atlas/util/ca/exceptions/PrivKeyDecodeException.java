package org.openstack.atlas.util.ca.exceptions;

public class PrivKeyDecodeException extends PrivKeyReaderException{

    public PrivKeyDecodeException() {
        super();
    }

    public PrivKeyDecodeException(String message) {
        super(message);
    }

    public PrivKeyDecodeException(String message, Throwable cause) {
        super(message,cause);
    }

    public PrivKeyDecodeException(Throwable cause) {
        super(cause);
    }

}
