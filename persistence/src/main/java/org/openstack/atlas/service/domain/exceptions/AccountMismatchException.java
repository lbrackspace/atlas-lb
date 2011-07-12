package org.openstack.atlas.service.domain.exceptions;

public class AccountMismatchException extends Exception {
    public AccountMismatchException(String message) {
        super(message);
    }
}