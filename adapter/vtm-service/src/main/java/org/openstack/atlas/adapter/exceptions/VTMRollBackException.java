package org.openstack.atlas.adapter.exceptions;

public class VTMRollBackException extends RollBackException {
    private static final long serialVersionUID = -1197590882399930192L;

    public VTMRollBackException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public VTMRollBackException(String message) {
        super(message);
    }
}
