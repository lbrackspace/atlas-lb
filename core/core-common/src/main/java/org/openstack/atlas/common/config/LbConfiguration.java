package org.openstack.atlas.common.config;

import org.openstack.atlas.common.config.osgi.cfg.commons.ApacheCommonsConfiguration;

public class LbConfiguration extends ApacheCommonsConfiguration {
    public static final String defaultConfigurationLocation = "/etc/openstack/atlas/public-api.conf";

    public LbConfiguration() {
        super(defaultConfigurationLocation);
    }
}