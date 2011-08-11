package org.openstack.atlas.adapter.exception;

public class RollbackException extends AdapterException {
	private static final long serialVersionUID = -1850300663038738768L;

    public RollbackException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
