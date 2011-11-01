package org.openstack.atlas.common.config;

public class ConfigurationNotFoundException extends ConfigurationInitializationException {

    public ConfigurationNotFoundException(String message) {
        super(message);
    }
}
