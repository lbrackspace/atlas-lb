package org.openstack.atlas.api.exceptions;

public class VTMTimeoutException extends RuntimeException {
    public VTMTimeoutException(String message) {
        super(message);
    }
}
