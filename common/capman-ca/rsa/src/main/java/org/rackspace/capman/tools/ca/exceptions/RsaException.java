package org.rackspace.capman.tools.ca.exceptions;

public class RsaException extends CapManUtilException {

    public RsaException() {
        super();
    }

    public RsaException(String msg){
        super(msg);
    }

    public RsaException(Throwable th){
        super(th);
    }
    public RsaException(String msg,Throwable th){
        super(msg,th);
    }
}
