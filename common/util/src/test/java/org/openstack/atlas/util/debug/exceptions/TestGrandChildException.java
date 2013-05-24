package org.openstack.atlas.util.debug.exceptions;

public class TestGrandChildException extends TestChildException {

    public TestGrandChildException(Throwable cause) {
        super(cause);
    }

    public TestGrandChildException(String message, Throwable cause) {
        super(message, cause);
    }

    public TestGrandChildException(String message) {
        super(message);
    }

    public TestGrandChildException() {
        super();
    }
}
