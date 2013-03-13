package org.openstack.atlas.util.common.exceptions;

public class ConverterException extends Exception {

    public ConverterException() {
        super();
    }

    public ConverterException(String msg) {
        super(msg);
    }

    public ConverterException(String msg,Throwable cause) {
        super(msg,cause);
    }

    public ConverterException(Throwable cause) {
        super(cause);
    }
}
