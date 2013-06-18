package org.openstack.atlas.adapter.exceptions;


public class RollBackException extends  Exception {
    private static final long serialVersionUID = -1197590882388830192L;

    public RollBackException(String s, Throwable throwable) {
        super(s, throwable);
    }

}
