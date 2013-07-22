
package org.rackspace.capman.tools.ca.exceptions;

public class RsaCsrException extends RsaException{

    public RsaCsrException(String msg, Throwable th) {
        super(msg,th);
    }

    public RsaCsrException(Throwable th) {
        super(th);
    }

    public RsaCsrException(String msg) {
        super(msg);
    }

    public RsaCsrException() {
        super();
    }

}
