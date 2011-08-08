package org.openstack.atlas.adapter.exceptions;

public class ObjectExistsException extends Exception {

	private static final long serialVersionUID = 3974478190890777924L;

	public ObjectExistsException(String message) {
		super(message);
	}

	public ObjectExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public ObjectExistsException(Throwable cause) {
		super(cause);
	}

}
