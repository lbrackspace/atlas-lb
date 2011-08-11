package org.openstack.atlas.adapter.exception;

public class AdapterException extends Exception {
    private static final long serialVersionUID = -1197590882399930192L;

    public AdapterException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
