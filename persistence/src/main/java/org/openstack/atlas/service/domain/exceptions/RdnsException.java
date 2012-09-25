package org.openstack.atlas.service.domain.exceptions;

public class RdnsException extends Exception {

    public RdnsException() {
        super();
    }

    public RdnsException(String msg){
        super(msg);
    }

    public RdnsException(Throwable th){
        super(th);
    }

    public RdnsException(String msg,Throwable th){
        super(msg,th);
    }
}
