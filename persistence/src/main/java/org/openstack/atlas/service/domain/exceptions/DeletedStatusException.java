package org.openstack.atlas.service.domain.exceptions;

public class DeletedStatusException extends Exception {
    public DeletedStatusException(String message) {
        super(message);
    }
}
