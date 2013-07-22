package org.rackspace.capman.tools.ca.exceptions;

public class ConversionException extends RsaException {

    public ConversionException() {
        super();
    }

    public ConversionException(String msg) {
        super(msg);
    }

    public ConversionException(Throwable th) {
        super(th);
    }

    public ConversionException(String msg, Throwable th) {
        super(msg, th);
    }
}
