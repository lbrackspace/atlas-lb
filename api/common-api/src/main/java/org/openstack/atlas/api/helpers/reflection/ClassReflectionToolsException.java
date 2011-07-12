package org.openstack.atlas.api.helpers.reflection;


public class ClassReflectionToolsException extends Exception {
    public ClassReflectionToolsException() {
        super();
    }

    public ClassReflectionToolsException(String msg){
        super(msg);
    }

    public ClassReflectionToolsException(Throwable ex) {
        super(ex);
    }

    public ClassReflectionToolsException(String msg,Throwable ex) {
        super(msg,ex);
    }
}
