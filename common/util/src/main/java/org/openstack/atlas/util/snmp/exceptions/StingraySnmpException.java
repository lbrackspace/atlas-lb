package org.openstack.atlas.util.snmp.exceptions;

public class StingraySnmpException extends Exception {
    public StingraySnmpException(String message) {
        super(message);
    }

    public StingraySnmpException(String message, Throwable throwable) {
        super(message, throwable);
    }
}