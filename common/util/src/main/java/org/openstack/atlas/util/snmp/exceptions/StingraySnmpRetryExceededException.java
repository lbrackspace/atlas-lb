
package org.openstack.atlas.util.snmp.exceptions;

public class StingraySnmpRetryExceededException extends StingraySnmpGeneralException {

    public StingraySnmpRetryExceededException() {
        super();
    }

    public StingraySnmpRetryExceededException(String message) {
        super(message);
    }

    public StingraySnmpRetryExceededException(String message, Throwable cause) {
        super(message, cause);
    }

    public StingraySnmpRetryExceededException(Throwable cause) {
        super(cause);
    }

}
