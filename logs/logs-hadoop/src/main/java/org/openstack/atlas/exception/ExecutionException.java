package org.openstack.atlas.exception;

public class ExecutionException extends Exception {

    public ExecutionException() {
        super();
    }

    public ExecutionException(String message) {
        super(message);
    }

    public ExecutionException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ExecutionException(Throwable throwable) {
        super(throwable);
    }
}
