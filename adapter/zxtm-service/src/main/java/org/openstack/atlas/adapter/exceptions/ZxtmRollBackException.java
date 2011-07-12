package org.openstack.atlas.adapter.exceptions;

public class ZxtmRollBackException extends Exception {
    private static final long serialVersionUID = -1197590882399930192L;

    public ZxtmRollBackException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
