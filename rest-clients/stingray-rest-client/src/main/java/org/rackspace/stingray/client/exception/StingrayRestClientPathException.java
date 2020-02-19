package org.rackspace.stingray.client.exception;

public class StingrayRestClientPathException extends Exception {

    private static final long serialVersionUID = -4300493802874418584L;

    public StingrayRestClientPathException(String message) {
        super(message);
    }

    public StingrayRestClientPathException(String msg, Throwable th) {
        super(msg, th);
    }

    public StingrayRestClientPathException(Throwable th) {
        super(th);
    }

    public StingrayRestClientPathException() {
        super();
    }



}
