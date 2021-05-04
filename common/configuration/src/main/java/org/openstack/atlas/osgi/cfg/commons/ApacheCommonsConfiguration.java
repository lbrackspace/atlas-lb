package org.openstack.atlas.osgi.cfg.commons;

import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.openstack.atlas.cfg.ConfigurationAccessException;
import org.openstack.atlas.cfg.ConfigurationInitializationException;
import org.openstack.atlas.cfg.ConfigurationKey;
import org.openstack.atlas.cfg.ConfigurationNotFoundException;
import org.openstack.atlas.cfg.Configuration;
import java.io.File;
import java.util.Iterator;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.PropertiesConfiguration;

public class ApacheCommonsConfiguration implements Configuration {

    private final File configurationFile;
    private long configurationFileLastModifiedTimestamp;
    private PropertiesConfiguration configuration;

    public ApacheCommonsConfiguration(String fileResourceLocation) {
        configurationFile = new File(fileResourceLocation);

        configurationFileLastModifiedTimestamp = 0L;
        configuration = null;
    }

    private boolean fileIsDifferentAndExists() {
        return configurationFile.length() != 0 && configurationFileLastModifiedTimestamp != configurationFile.lastModified();
    }

    //TODO: Behavior for non-existent configurations should be passive?
    private synchronized void checkState() throws ConfigurationInitializationException {
        if (configuration == null || fileIsDifferentAndExists()) {
            try {
                if (!configurationFile.exists()) {
                    throw new ConfigurationNotFoundException("Unable to locate file: " + configurationFile.getPath());
                } else if (!configurationFile.canRead()) {
                    throw new ConfigurationAccessException("Insufficient permissions to read file: " + configurationFile.getPath());
                }
            } catch (ConfigurationInitializationException cie) {
                //Got to love piggy back logic that breaks programatic flow
                configuration = new PropertiesConfiguration();

                throw cie;
            } finally {
                configurationFileLastModifiedTimestamp = configurationFile.lastModified();
            }

            try {
                FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                        new FileBasedConfigurationBuilder<PropertiesConfiguration>(PropertiesConfiguration.class)
                                .configure(new Parameters().properties()
                                        .setFile(configurationFile));
                configuration = builder.getConfiguration();
            } catch (ConfigurationException ce) {
                throw new ConfigurationInitializationException(ce.getMessage(), ce.getCause());
            }
        }
    }

    public boolean isModified() {
        return configurationFileLastModifiedTimestamp != configurationFile.lastModified();
    }

    @Override
    public String getString(ConfigurationKey key) throws ConfigurationInitializationException {
        checkState();

        return configuration.getString(key.name());
    }

    @Override
    public boolean hasKeys(ConfigurationKey... keys) throws ConfigurationInitializationException {
        checkState();

        boolean okay = true;

        for (ConfigurationKey key : keys) {
            okay = configuration.containsKey(key.name());

            if (!okay) {
                break;
            }
        }

        return okay;
    }

    @Override
    public Iterator getKeys() {
        checkState();
        return configuration.getKeys();
    }

    @Override
    public String getString(String key) throws ConfigurationInitializationException {
        checkState();
        return configuration.getString(key);
    }
}
