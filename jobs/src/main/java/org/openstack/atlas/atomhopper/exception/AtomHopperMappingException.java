package org.openstack.atlas.atomhopper.exception;

public class AtomHopperMappingException extends Exception {

    public AtomHopperMappingException() {
    }

    public AtomHopperMappingException(String s) {
        super(s);
    }

    public AtomHopperMappingException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public AtomHopperMappingException(Throwable throwable) {
        super(throwable);
    }
}
