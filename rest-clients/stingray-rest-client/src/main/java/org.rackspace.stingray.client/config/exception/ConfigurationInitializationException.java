package org.rackspace.stingray.client.config.exception;

public class ConfigurationInitializationException extends RuntimeException {

    public ConfigurationInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationInitializationException(String message) {
        super(message);
    }
}