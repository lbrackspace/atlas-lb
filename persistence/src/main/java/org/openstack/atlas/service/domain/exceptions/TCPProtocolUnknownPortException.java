package org.openstack.atlas.service.domain.exceptions;

public class TCPProtocolUnknownPortException extends Exception {
    public TCPProtocolUnknownPortException(String message) {
        super(message);
    }
}