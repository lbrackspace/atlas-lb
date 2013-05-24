
package org.openstack.atlas.util.debug.exceptions;

public class DebugTestBaseException extends Exception{

    public DebugTestBaseException(Throwable cause) {
        super(cause);
    }

    public DebugTestBaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DebugTestBaseException(String message) {
        super(message);
    }

    public DebugTestBaseException() {
        super();
    }

}
