package org.openstack.atlas.service.domain.exceptions;

public class SingletonEntityAlreadyExistsException extends Exception {
    public SingletonEntityAlreadyExistsException(String message) {
        super(message);
    }
}
