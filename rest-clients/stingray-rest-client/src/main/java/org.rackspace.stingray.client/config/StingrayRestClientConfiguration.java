package org.rackspace.stingray.client.config;


public class StingrayRestClientConfiguration extends ApacheCommonsConfiguration {
    public static final String defaultConfigurationLocation = "/etc/stingray/stingray-client.conf";

    public StingrayRestClientConfiguration() {
        super(defaultConfigurationLocation);
    }
}
