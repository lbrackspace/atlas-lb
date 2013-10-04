package org.rackspace.stingray.client.exception;

public class StingrayRestClientException extends Exception {

    private static final long serialVersionUID = -1984130663036438322L;

    public StingrayRestClientException(String message) {
        super(message);
    }

    public StingrayRestClientException(String msg, Throwable th) {
        super(msg, th);
    }

    public StingrayRestClientException(Throwable th) {
        super(th);
    }

    public StingrayRestClientException() {
        super();
    }
}
