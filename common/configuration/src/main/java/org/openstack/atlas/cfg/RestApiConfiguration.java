package org.openstack.atlas.cfg;

import org.openstack.atlas.osgi.cfg.commons.ApacheCommonsConfiguration;
import org.springframework.stereotype.Component;

@Component
public class RestApiConfiguration extends ApacheCommonsConfiguration {

    public static final String defaultConfigurationLocation = "/etc/openstack/atlas/public-api.conf";

    public RestApiConfiguration() {
        super(defaultConfigurationLocation);
    }

    public RestApiConfiguration(String configurationLocation) {
        super(configurationLocation);
    }
}
