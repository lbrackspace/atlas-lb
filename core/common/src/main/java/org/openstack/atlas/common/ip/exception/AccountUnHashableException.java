package org.openstack.atlas.common.ip.exception;

public class AccountUnHashableException extends IPStringConversionException1 {

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
