package org.openstack.atlas.util.debug.exceptions;

public class DebugTestChildException extends DebugTestBaseException {

    public DebugTestChildException() {
        super();
    }

    public DebugTestChildException(String message) {
        super(message);
    }

    public DebugTestChildException(String message, Throwable cause) {
        super(message,cause);
    }

    public DebugTestChildException(Throwable cause) {
        super(cause);
    }
}
