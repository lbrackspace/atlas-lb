package org.rackspace.capman.tools.util;

import java.util.List;

public class ResponseWithExcpetions<T> {

    private List<? extends Throwable> exceptions;
    private T returnObject;

    public ResponseWithExcpetions(List<? extends Throwable> exceptions, T response) {
        this.returnObject = response;
        this.exceptions = exceptions;
    }

    public List<? extends Throwable> getExceptions() {
        return exceptions;
    }

    public T getReturnObject() {
        return returnObject;
    }

    public void setReturnObject(T returnObject) {
        this.returnObject = returnObject;
    }
}
