package org.openstack.atlas.service.domain.exception;

public class NoMappableConstantException extends RuntimeException {
    public NoMappableConstantException(String message) {
        super(message);
    }
}
