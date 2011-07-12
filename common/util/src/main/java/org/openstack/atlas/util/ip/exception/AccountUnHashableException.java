package org.openstack.atlas.util.ip.exception;

public class AccountUnHashableException extends IPStringConversionException {

    public AccountUnHashableException() {
        super();
    }

    public AccountUnHashableException(String msg) {
        super(msg);
    }

    public AccountUnHashableException(Throwable th) {
        super(th);
    }

    public AccountUnHashableException(String msg,Throwable th) {
        super(msg,th);
    }
}
