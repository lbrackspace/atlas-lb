package org.openstack.atlas.service.domain.exception;

public class LimitReachedException extends Exception {
    public LimitReachedException(String message) {
        super(message);
    }
}
