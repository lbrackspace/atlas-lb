package org.rackspace.capman.tools.ca.exceptions;

public class NotAnX509CertificateException extends X509ReaderException {

    public NotAnX509CertificateException() {
        super();
    }

    public NotAnX509CertificateException(String msg) {
        super(msg);
    }

    public NotAnX509CertificateException(Throwable th) {
        super(th);
    }

    public NotAnX509CertificateException(String msg, Throwable th) {
        super(msg, th);
    }
}
