package org.rackspace.vtm.client.config;

import org.rackspace.vtm.client.config.exception.ConfigurationInitializationException;

public interface Configuration {

    String getString(ConfigurationKey key) throws ConfigurationInitializationException;

    boolean hasKeys(ConfigurationKey... keys) throws ConfigurationInitializationException;
}