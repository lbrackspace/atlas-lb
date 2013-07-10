package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.osgi.cfg.commons.ApacheCommonsConfiguration;

public class LoadBalancingConfig extends ApacheCommonsConfiguration {
    public static final String defaultConfigurationLocation = "/etc/openstack/atlas/loadbalancing.hibernate.conf";

    public LoadBalancingConfig() {
        super(defaultConfigurationLocation);
    }
}
