package org.rackspace.vtm.client.config;


public class VTMRestClientConfiguration extends ApacheCommonsConfiguration {
    public static final String defaultConfigurationLocation = "/etc/openstack/atlas/stingray-client.conf";

    public VTMRestClientConfiguration() {
        super(defaultConfigurationLocation);
    }
}
