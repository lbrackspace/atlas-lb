package org.openstack.atlas.service.domain.exception;

public class AccountMismatchException extends Exception {
    public AccountMismatchException(String message) {
        super(message);
    }
}