package org.rackspace.vtm.client.config;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.rackspace.vtm.client.config.exception.ConfigurationAccessException;
import org.rackspace.vtm.client.config.exception.ConfigurationInitializationException;

import java.io.File;

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
                if (configurationFile.exists()) {
                    if (configurationFile.canRead()) {
                        configurationFileLastModifiedTimestamp = configurationFile.lastModified();
                    } else {
                        configuration = new PropertiesConfiguration();
                        throw new ConfigurationAccessException("Insufficient permission to read file: " + configurationFile.getPath());
                    }
                } else {
                    configuration = new PropertiesConfiguration();
                    throw new ConfigurationAccessException("Unable to locate file: " + configurationFile.getPath());
                }
            } catch (ConfigurationInitializationException cie) {
                throw cie;
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
                return okay;
            }
        }

        return okay;
    }
}
