package org.rackspace.capman.tools.ca.exceptions;

public class PemException extends RsaException {
    public PemException(){
    }
    public PemException(String msg){
        super(msg);
    }
    public PemException(Throwable th){
        super(th);
    }
    public PemException(String msg,Throwable th){
        super(msg,th);
    }
}
