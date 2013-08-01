package org.openstack.atlas.api.config;

import org.openstack.atlas.osgi.cfg.commons.ApacheCommonsConfiguration;

public class RestApiConfiguration extends ApacheCommonsConfiguration {
    public static final String defaultConfigurationLocation = "/etc/openstack/atlas/public-api.conf";

    public RestApiConfiguration() {
        super(defaultConfigurationLocation);
    }
}
