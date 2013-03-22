package org.openstack.atlas.util.snmp.exceptions;

public class StingraySnmpGeneralException extends Exception {

    public StingraySnmpGeneralException(Throwable cause) {
        super(cause);
    }

    public StingraySnmpGeneralException(String message, Throwable cause) {
        super(message, cause);
    }

    public StingraySnmpGeneralException(String message) {
        super(message);
    }

    public StingraySnmpGeneralException() {
    }

}