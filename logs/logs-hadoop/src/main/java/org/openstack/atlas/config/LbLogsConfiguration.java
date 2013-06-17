package org.openstack.atlas.config;

import org.openstack.atlas.osgi.cfg.commons.ApacheCommonsConfiguration;

public class LbLogsConfiguration extends ApacheCommonsConfiguration {

    public static final String defaultConfigurationLocation = "/etc/openstack/atlas/hadoop-logs.conf";

    public LbLogsConfiguration() {
        super(defaultConfigurationLocation);
    }

    public LbLogsConfiguration(String configurationFileName) {
        super(configurationFileName);
    }
}
