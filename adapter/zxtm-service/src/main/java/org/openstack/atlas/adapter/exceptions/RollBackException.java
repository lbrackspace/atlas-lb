package org.openstack.atlas.adapter.exceptions;


public class RollBackException extends  Exception {
    private static final long serialVersionUID = -1197590882388830192L;

      public RollBackException(Throwable cause) {
        super(cause);
    }

    public RollBackException(String message, Throwable cause) {
        super(message,cause);
    }

    public RollBackException(String message) {
        super(message);
    }

    public RollBackException() {
        super();
    }

}
