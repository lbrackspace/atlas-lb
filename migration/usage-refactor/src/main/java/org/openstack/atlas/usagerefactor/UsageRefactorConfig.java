package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.cfg.ConfigurationInitializationException;
import org.openstack.atlas.osgi.cfg.commons.ApacheCommonsConfiguration;

import java.util.Iterator;

public class UsageRefactorConfig extends ApacheCommonsConfiguration {
    public static final String defaultConfigurationLocation = "/etc/openstack/atlas/usagerefactor.hibernate.conf";

    public UsageRefactorConfig() {
        super(defaultConfigurationLocation);
    }
}
