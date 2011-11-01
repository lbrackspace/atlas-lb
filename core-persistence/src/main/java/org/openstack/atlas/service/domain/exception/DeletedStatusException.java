package org.openstack.atlas.service.domain.exception;

public class DeletedStatusException extends Exception {
    public DeletedStatusException(String message) {
        super(message);
    }
}
