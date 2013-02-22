
package org.openstack.atlas.util.sequencefile;

public class EndOfIteratorException extends Exception{
    public EndOfIteratorException(Throwable cause) {
        super(cause);
    }

    public EndOfIteratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public EndOfIteratorException(String message) {
        super(message);
    }

    public EndOfIteratorException() {
        super();
    }
}
