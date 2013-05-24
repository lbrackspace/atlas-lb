package org.openstack.atlas.adapter.exceptions;

public class ZxtmRollBackException extends Exception {

    public ZxtmRollBackException(Throwable cause) {
        super(cause);
    }

    public ZxtmRollBackException(String message, Throwable cause) {
        super(message,cause);
    }

    public ZxtmRollBackException(String message) {
        super(message);
    }

    public ZxtmRollBackException() {
        super();
    }
    private static final long serialVersionUID = -1197590882399930192L;

}
