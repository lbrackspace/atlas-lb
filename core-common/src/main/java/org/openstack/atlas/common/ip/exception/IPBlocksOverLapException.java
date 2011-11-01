package org.openstack.atlas.common.ip.exception;

public class IPBlocksOverLapException extends IPStringException {

    public IPBlocksOverLapException() {
    }

    public IPBlocksOverLapException(String msg) {
        super(msg);
    }
}
