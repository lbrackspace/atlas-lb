package org.openstack.atlas.adapter.exceptions;

public class StmRollBackException extends Exception {
    private static final long serialVersionUID = -1197590882399930192L;

    public StmRollBackException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
