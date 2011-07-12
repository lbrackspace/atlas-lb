package org.openstack.atlas.service.domain.exceptions;

public class LimitReachedException extends Exception {
    public LimitReachedException(String message) {
        super(message);
    }
}
