package org.openstack.atlas.cfg;

public class ConfigurationNotFoundException extends ConfigurationInitializationException {

    public ConfigurationNotFoundException(String message) {
        super(message);
    }
}
