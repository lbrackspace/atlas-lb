package org.rackspace.vtm.client.exception;

public class VTMRestClientException extends Exception {

    private static final long serialVersionUID = 2713880240957290577L;

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
