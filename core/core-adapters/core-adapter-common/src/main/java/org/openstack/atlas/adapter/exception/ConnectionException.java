package org.openstack.atlas.adapter.exception;

public class ConnectionException extends AdapterException {
    private static final long serialVersionUID = -1197590882399930192L;

    public ConnectionException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(Throwable throwable) {
        super(throwable);
    }
}
