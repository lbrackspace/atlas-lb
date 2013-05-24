
package org.openstack.atlas.util.debug.exceptions;

public class TestSomeOtherException extends Exception{

    public TestSomeOtherException(Throwable cause) {
        super(cause);
    }

    public TestSomeOtherException(String message, Throwable cause) {
        super(message, cause);
    }

    public TestSomeOtherException(String message) {
        super(message);
    }

    public TestSomeOtherException() {
        super();
    }
}
