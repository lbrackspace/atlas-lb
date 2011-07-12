package org.openstack.atlas.service.domain.exceptions;

public class TransactionFailedException extends Exception {
    public TransactionFailedException(String message) {
        super(message);
    }
}
