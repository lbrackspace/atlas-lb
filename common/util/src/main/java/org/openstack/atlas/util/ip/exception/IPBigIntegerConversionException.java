package org.openstack.atlas.util.ip.exception;

public class IPBigIntegerConversionException extends IPException {

    public IPBigIntegerConversionException() {
        super();
    }

    public IPBigIntegerConversionException(String msg) {
        super(msg);
    }

    public IPBigIntegerConversionException(String msg,Throwable th) {
        super(msg,th);
    }

    public IPBigIntegerConversionException(Throwable th) {
        super(th);
    }
}
