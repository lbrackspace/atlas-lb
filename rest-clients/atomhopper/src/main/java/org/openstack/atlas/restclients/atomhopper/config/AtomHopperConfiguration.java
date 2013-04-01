package org.openstack.atlas.restclients.atomhopper.config;

import org.openstack.atlas.osgi.cfg.commons.ApacheCommonsConfiguration;

public class AtomHopperConfiguration extends ApacheCommonsConfiguration {
    public static final String defaultConfigurationLocation = "/etc/openstack/atlas/public-api.conf";

    public AtomHopperConfiguration() {
        super(defaultConfigurationLocation);
    }
}