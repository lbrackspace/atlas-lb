package org.openstack.atlas.common.config;

public class ConfigurationInitializationException extends RuntimeException {

    public ConfigurationInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationInitializationException(String message) {
        super(message);
    }
}
