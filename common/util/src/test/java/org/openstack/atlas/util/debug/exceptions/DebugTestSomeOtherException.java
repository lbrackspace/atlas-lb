
package org.openstack.atlas.util.debug.exceptions;

public class DebugTestSomeOtherException extends Exception{

    public DebugTestSomeOtherException(Throwable cause) {
        super(cause);
    }

    public DebugTestSomeOtherException(String message, Throwable cause) {
        super(message, cause);
    }

    public DebugTestSomeOtherException(String message) {
        super(message);
    }

    public DebugTestSomeOtherException() {
        super();
    }
}
