package org.openstack.atlas.util.ip.exception;

public class IPException extends Exception {

    public IPException() {
    }

    public IPException(String msg){
        super(msg);
    }

    public IPException(Throwable th){
        super(th);
    }

    public IPException(String msg,Throwable th){
        super(msg,th);
    }
}
