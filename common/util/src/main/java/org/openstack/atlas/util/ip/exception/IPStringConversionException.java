package org.openstack.atlas.util.ip.exception;

public class IPStringConversionException extends IPException {

    public IPStringConversionException() {
        super();
    }

    public IPStringConversionException(String msg) {
        super(msg);
    }

    public IPStringConversionException(String msg,Throwable th) {
        super(msg,th);
    }

    public IPStringConversionException(Throwable th) {
        super(th);
    }
}
