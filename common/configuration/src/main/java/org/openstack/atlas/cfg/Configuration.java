package org.openstack.atlas.cfg;

public interface Configuration {

    String getString(ConfigurationKey key) throws ConfigurationInitializationException;

    boolean hasKeys(ConfigurationKey... keys) throws ConfigurationInitializationException;
}
