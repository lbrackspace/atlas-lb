package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.osgi.cfg.commons.ApacheCommonsConfiguration;

public class LoadbalancingUsageConfig extends ApacheCommonsConfiguration {
    public static final String defaultConfigurationLocation = "/etc/openstack/atlas/loadbalancing_usage.hibernate.conf";

    public LoadbalancingUsageConfig() {
        super(defaultConfigurationLocation);
    }
}
