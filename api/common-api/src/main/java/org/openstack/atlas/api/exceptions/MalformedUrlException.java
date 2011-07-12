package org.openstack.atlas.api.exceptions;

public class MalformedUrlException extends RuntimeException {
    public MalformedUrlException(String message) {
        super(message);
    }
}
