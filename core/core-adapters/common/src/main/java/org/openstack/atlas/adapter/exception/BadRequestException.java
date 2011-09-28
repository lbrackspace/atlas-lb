package org.openstack.atlas.adapter.exception;

public class BadRequestException extends AdapterException {
    private static final long serialVersionUID = -1197590882399930192L;

    public BadRequestException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
