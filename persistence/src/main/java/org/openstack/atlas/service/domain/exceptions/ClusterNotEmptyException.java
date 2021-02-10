package org.openstack.atlas.service.domain.exceptions;

public class ClusterNotEmptyException extends Exception {
    public ClusterNotEmptyException(String message) {
        super(message);
    }
}
