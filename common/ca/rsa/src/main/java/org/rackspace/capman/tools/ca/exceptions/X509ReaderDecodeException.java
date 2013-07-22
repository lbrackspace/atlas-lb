
package org.rackspace.capman.tools.ca.exceptions;

public class X509ReaderDecodeException extends X509ReaderException{

    public X509ReaderDecodeException(String msg, Throwable th) {
        super(msg,th);
    }

    public X509ReaderDecodeException(Throwable th) {
        super(th);
    }

    public X509ReaderDecodeException(String msg) {
        super(msg);
    }

    public X509ReaderDecodeException() {
        super();
    }

}
