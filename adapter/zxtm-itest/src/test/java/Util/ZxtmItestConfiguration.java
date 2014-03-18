package Util;

import org.openstack.atlas.osgi.cfg.commons.ApacheCommonsConfiguration;

public class ZxtmItestConfiguration extends ApacheCommonsConfiguration {
    public static final String defaultConfigurationLocation = "/etc/openstack/atlas/test/zxtm-adapter-itest.conf";

    public ZxtmItestConfiguration() {
        super(defaultConfigurationLocation);
    }
}