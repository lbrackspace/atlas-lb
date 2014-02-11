package org.openstack.atlas.restclients.atomhopper.exception;

public class ClientInstantiationException extends Exception {
    public ClientInstantiationException(String s) {
        super(s);
    }

    public ClientInstantiationException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
