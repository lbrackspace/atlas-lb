package org.openstack.atlas.service.domain.exceptions;

public class NoMappableConstantException extends RuntimeException {
    public NoMappableConstantException(String message) {
        super(message);
    }
}
