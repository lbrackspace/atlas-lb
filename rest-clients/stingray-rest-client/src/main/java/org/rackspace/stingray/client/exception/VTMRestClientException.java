package org.rackspace.stingray.client.exception;

public class VTMRestClientException extends Exception {

    private static final long serialVersionUID = -1984130663036438322L;

    public VTMRestClientException(String message) {
        super(message);
    }

    public VTMRestClientException(String msg, Throwable th) {
        super(msg, th);
    }

    public VTMRestClientException(Throwable th) {
        super(th);
    }

    public VTMRestClientException() {
        super();
    }
}
