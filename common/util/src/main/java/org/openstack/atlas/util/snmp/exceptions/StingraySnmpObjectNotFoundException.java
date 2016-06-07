package org.openstack.atlas.util.snmp.exceptions;

public class StingraySnmpObjectNotFoundException extends StingraySnmpGeneralException{

    public StingraySnmpObjectNotFoundException() {
        super();
    }

    public StingraySnmpObjectNotFoundException(String message) {
        super(message);
    }

    public StingraySnmpObjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public StingraySnmpObjectNotFoundException(Throwable cause) {
        super(cause);
    }

}
