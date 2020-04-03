package org.rackspace.vtm.client.exception;

public class VTMRestClientPathException extends Exception {

    private static final long serialVersionUID = 8536554624744721098L;

    public VTMRestClientPathException(String message) {
        super(message);
    }

    public VTMRestClientPathException(String msg, Throwable th) {
        super(msg, th);
    }

    public VTMRestClientPathException(Throwable th) {
        super(th);
    }

    public VTMRestClientPathException() {
        super();
    }



}
