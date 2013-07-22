
package org.rackspace.capman.tools.ca.exceptions;

public class X509ReaderNoSuchExtensionException extends X509ReaderException{

    public X509ReaderNoSuchExtensionException(String msg, Throwable th) {
        super(msg, th);
    }

    public X509ReaderNoSuchExtensionException(Throwable th) {
        super(th);
    }

    public X509ReaderNoSuchExtensionException(String msg) {
        super(msg);
    }

    public X509ReaderNoSuchExtensionException() {
    }

}
