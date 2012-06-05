package org.openstack.atlas.atom.config;

import org.openstack.atlas.osgi.cfg.commons.ApacheCommonsConfiguration;
public class AtomHopperConfiguration extends ApacheCommonsConfiguration {
    public static final String defaultConfigurationLocation = "/etc/openstack/atlas/public-api.conf";

    public AtomHopperConfiguration() {
        super(defaultConfigurationLocation);
    }
}