package org.openstack.atlas.api.helpers;

import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.Configuration;

public final class ConfigurationHelper {
    public static boolean isAllowed(Configuration configuration, PublicApiServiceConfigurationKeys key) {
        //Verify the users request is allowed...
            return (configuration.hasKeys(key)
                    && configuration.getString(key).toLowerCase().equals("true"));
    }
}
