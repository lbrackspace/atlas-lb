package org.rackspace.capman.tools.ca.exceptions;

public class NotAPemObject extends PemException {

    public NotAPemObject() {
    }

    public NotAPemObject(String msg) {
        super(msg);
    }

    public NotAPemObject(Throwable th) {
        super(th);
    }

    public NotAPemObject(String msg, Throwable th) {
        super(msg, th);
    }
}
