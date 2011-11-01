package org.openstack.atlas.common.ip.exception;

public class IPCidrBlockOutOfRangeException extends IPStringException {

    public IPCidrBlockOutOfRangeException() {
    }

    public IPCidrBlockOutOfRangeException(String msg) {
        super(msg);
    }
}
