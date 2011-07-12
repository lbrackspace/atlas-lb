package org.openstack.atlas.usage.helpers;

import org.openstack.atlas.osgi.cfg.commons.ApacheCommonsConfiguration;

public class EsbConfiguration extends ApacheCommonsConfiguration {
    public static final String defaultConfigurationLocation = "/etc/openstack/atlas/public-api.conf";

    public EsbConfiguration() {
        super(defaultConfigurationLocation);
    }
}