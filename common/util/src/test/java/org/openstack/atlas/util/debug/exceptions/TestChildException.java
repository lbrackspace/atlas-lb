package org.openstack.atlas.util.debug.exceptions;

public class TestChildException extends TestBaseException {

    public TestChildException() {
        super();
    }

    public TestChildException(String message) {
        super(message);
    }

    public TestChildException(String message, Throwable cause) {
        super(message,cause);
    }

    public TestChildException(Throwable cause) {
        super(cause);
    }
}
