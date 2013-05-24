
package org.openstack.atlas.util.debug.exceptions;

public class TestBaseException extends Exception{

    public TestBaseException(Throwable cause) {
        super(cause);
    }

    public TestBaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public TestBaseException(String message) {
        super(message);
    }

    public TestBaseException() {
        super();
    }

}
