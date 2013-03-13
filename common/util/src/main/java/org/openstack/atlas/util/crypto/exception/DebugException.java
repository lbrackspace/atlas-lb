package org.openstack.atlas.util.crypto.exception;

public class DebugException extends Exception{

    public DebugException(Throwable cause) {
        super(cause);
    }

    public DebugException(String message, Throwable cause) {
        super(message, cause);
    }

    public DebugException(String message) {
        super(message);
    }

    public DebugException() {
    }

}
