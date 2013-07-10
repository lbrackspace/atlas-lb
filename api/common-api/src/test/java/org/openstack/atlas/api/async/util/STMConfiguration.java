package org.openstack.atlas.api.async.util;

import org.openstack.atlas.osgi.cfg.commons.ApacheCommonsConfiguration;

public class STMConfiguration extends ApacheCommonsConfiguration {
    public static final String defaultConfigurationLocation = "/etc/openstack/atlas/stingray-client-test.conf";


    public STMConfiguration() {
        super(defaultConfigurationLocation);
    }
}