package org.openstack.atlas.cfg;

public class ConfigurationInitializationException extends RuntimeException {

    public ConfigurationInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationInitializationException(String message) {
        super(message);
    }
}
