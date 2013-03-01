
package org.openstack.atlas.logs.hadoop.sequencefiles;

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
