package Util;

import org.openstack.atlas.osgi.cfg.commons.ApacheCommonsConfiguration;

public class EsbConfiguration extends ApacheCommonsConfiguration {
    public static final String defaultConfigurationLocation = "/var/lib/jenkins/openstack/atlas/public-api-test.conf";

    public EsbConfiguration() {
        super(defaultConfigurationLocation);
    }
}