package org.rackspace.capman.tools.ca.exceptions;

public class FileUtilsException extends Exception {

    public FileUtilsException(Throwable cause) {
        super(cause);
    }

    public FileUtilsException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileUtilsException(String message) {
        super(message);
    }

    public FileUtilsException() {
    }

}
