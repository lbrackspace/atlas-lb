package org.openstack.atlas.exception;

public class SchedulingException extends Exception {

    public SchedulingException(){
        super();
    }

    public SchedulingException(String message) {
        super(message);
    }

    public SchedulingException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public SchedulingException(Throwable throwable) {
        super(throwable);
    }
}
