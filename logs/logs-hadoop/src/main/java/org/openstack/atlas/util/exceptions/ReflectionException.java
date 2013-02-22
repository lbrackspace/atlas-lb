
package org.openstack.atlas.util.exceptions;

/**
 *
 * @author crc
 */
public class ReflectionException extends Exception{
    public ReflectionException(Throwable cause) {
        super(cause);
    }

    public ReflectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReflectionException(String message) {
        super(message);
    }

    public ReflectionException() {
        super();
    }
}
