package org.openstack.atlas.adapter.exception;

public class ConnectionException extends AdapterException {
    private static final long serialVersionUID = -1197590882399930192L;

    public ConnectionException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
