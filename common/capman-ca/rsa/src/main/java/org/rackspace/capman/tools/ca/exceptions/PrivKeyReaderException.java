
package org.rackspace.capman.tools.ca.exceptions;

public class PrivKeyReaderException extends CapManUtilException{
    public PrivKeyReaderException(Throwable cause) {
        super(cause);
    }

    public PrivKeyReaderException(String message, Throwable cause) {
        super(message,cause);
    }

    public PrivKeyReaderException(String message) {
        super(message);
    }

    public PrivKeyReaderException() {
        super();
    }

}
