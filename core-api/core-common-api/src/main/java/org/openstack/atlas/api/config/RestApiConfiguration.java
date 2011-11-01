package org.openstack.atlas.api.config;

import org.openstack.atlas.common.config.osgi.cfg.commons.ApacheCommonsConfiguration;
import org.springframework.stereotype.Component;

@Component
public class RestApiConfiguration extends ApacheCommonsConfiguration {
    public static final String defaultConfigurationLocation = "/etc/openstack/atlas/public-api.conf";

    public RestApiConfiguration() {
        super(defaultConfigurationLocation);
    }
}
