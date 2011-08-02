package org.openstack.atlas.adapter.exceptions;

public class InvalidObjectNameException extends Exception {

	private static final long serialVersionUID = 7374478190890888952L;

	public InvalidObjectNameException(String message) {
		super(message);
	}

	public InvalidObjectNameException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidObjectNameException(Throwable cause) {
		super(cause);
	}

}
