package org.rackspace.stingray.client.config;

import org.rackspace.stingray.client.config.exception.ConfigurationInitializationException;

public interface Configuration {

    String getString(ConfigurationKey key) throws ConfigurationInitializationException;

    boolean hasKeys(ConfigurationKey... keys) throws ConfigurationInitializationException;
}