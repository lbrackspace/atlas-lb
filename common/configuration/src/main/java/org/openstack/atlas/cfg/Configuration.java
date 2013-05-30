package org.openstack.atlas.cfg;

import java.util.Iterator;

public interface Configuration {

    String getString(ConfigurationKey key) throws ConfigurationInitializationException;

    boolean hasKeys(ConfigurationKey... keys) throws ConfigurationInitializationException;

    Iterator getKeys();

    String getString(String key) throws ConfigurationInitializationException;
}
