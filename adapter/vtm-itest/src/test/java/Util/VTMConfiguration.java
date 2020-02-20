package Util;

import org.openstack.atlas.osgi.cfg.commons.ApacheCommonsConfiguration;

public class VTMConfiguration extends ApacheCommonsConfiguration {
    public static final String defaultConfigurationLocation = "/etc/openstack/atlas/stingray-client.conf";


    public VTMConfiguration() {
        super(defaultConfigurationLocation);
    }
}