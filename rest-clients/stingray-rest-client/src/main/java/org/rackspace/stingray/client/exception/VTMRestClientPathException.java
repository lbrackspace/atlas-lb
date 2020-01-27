package org.rackspace.stingray.client.exception;

public class VTMRestClientPathException extends Exception {

    private static final long serialVersionUID = -1984130663036438452L;

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
