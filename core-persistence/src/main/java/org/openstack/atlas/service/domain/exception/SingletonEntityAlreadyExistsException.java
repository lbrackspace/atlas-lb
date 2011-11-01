package org.openstack.atlas.service.domain.exception;

public class SingletonEntityAlreadyExistsException extends Exception {
    public SingletonEntityAlreadyExistsException(String message) {
        super(message);
    }
}
