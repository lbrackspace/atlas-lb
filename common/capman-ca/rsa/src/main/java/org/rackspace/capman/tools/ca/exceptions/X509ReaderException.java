package org.rackspace.capman.tools.ca.exceptions;

public class X509ReaderException extends CapManUtilException {
    public X509ReaderException(){
        super();
    }

    public X509ReaderException(String msg){
        super(msg);
    }

    public X509ReaderException(Throwable th){
        super(th);
    }

    public X509ReaderException(String msg,Throwable th){
        super(msg,th);
    }
}
