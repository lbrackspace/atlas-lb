package org.openstack.atlas.api.mgmt.config;

import org.openstack.atlas.osgi.cfg.commons.ApacheCommonsConfiguration;

public class ManagementApiConfiguration extends ApacheCommonsConfiguration {
    public static final String defaultConfigurationLocation = "/etc/openstack/atlas/management-api.conf";

    public ManagementApiConfiguration() {
        super(defaultConfigurationLocation);
    }
}
