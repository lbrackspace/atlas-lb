package org.openstack.atlas.service.domain.exception;

public class UniqueLbPortViolationException extends Exception {
    public UniqueLbPortViolationException(String message) {
        super(message);
    }
}