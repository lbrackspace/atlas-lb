package org.openstack.atlas.adapter.exception;

public class AdapterException extends Exception {
    private static final long serialVersionUID = -1197590882399930192L;

    public AdapterException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public AdapterException(String message) {
        super(message);
    }

    public AdapterException(Throwable throwable) {
        super(throwable);
    }
}
