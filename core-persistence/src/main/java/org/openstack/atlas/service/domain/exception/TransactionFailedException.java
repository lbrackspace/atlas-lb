package org.openstack.atlas.service.domain.exception;

public class TransactionFailedException extends Exception {
    public TransactionFailedException(String message) {
        super(message);
    }
}
