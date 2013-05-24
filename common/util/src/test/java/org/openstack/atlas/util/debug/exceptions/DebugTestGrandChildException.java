package org.openstack.atlas.util.debug.exceptions;

public class DebugTestGrandChildException extends DebugTestChildException {

    public DebugTestGrandChildException(Throwable cause) {
        super(cause);
    }

    public DebugTestGrandChildException(String message, Throwable cause) {
        super(message, cause);
    }

    public DebugTestGrandChildException(String message) {
        super(message);
    }

    public DebugTestGrandChildException() {
        super();
    }
}
