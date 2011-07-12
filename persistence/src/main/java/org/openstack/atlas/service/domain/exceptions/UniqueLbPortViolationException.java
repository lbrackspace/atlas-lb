package org.openstack.atlas.service.domain.exceptions;

public class UniqueLbPortViolationException extends Exception {
    public UniqueLbPortViolationException(String message) {
        super(message);
    }
}