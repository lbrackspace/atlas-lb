package org.openstack.atlas.restclients.auth.config;

import org.openstack.atlas.osgi.cfg.commons.ApacheCommonsConfiguration;

public class AuthenticationCredentialConfiguration extends ApacheCommonsConfiguration {
    public static final String defaultConfigurationLocation = "/etc/openstack/atlas/public-api.conf";

    public AuthenticationCredentialConfiguration() {
        super(defaultConfigurationLocation);
    }
}