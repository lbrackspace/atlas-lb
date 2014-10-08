package org.openstack.atlas.service.domain.exceptions;

public class NoAvailableClusterException extends Exception {
    public NoAvailableClusterException(String message) {
        super(message);
    }
}
