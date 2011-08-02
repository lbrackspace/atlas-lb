package org.openstack.atlas.adapter.exceptions;

public class AdapterRollBackException extends Exception {
    private static final long serialVersionUID = -1197590882399930192L;

    public AdapterRollBackException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
