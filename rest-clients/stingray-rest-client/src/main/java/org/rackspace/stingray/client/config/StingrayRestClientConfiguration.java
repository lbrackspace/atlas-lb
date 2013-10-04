package org.rackspace.stingray.client.config;


public class StingrayRestClientConfiguration extends ApacheCommonsConfiguration {
    public static final String defaultConfigurationLocation = "/etc/openstack/atlas/stingray-client.conf";

    public StingrayRestClientConfiguration() {
        super(defaultConfigurationLocation);
    }
}
