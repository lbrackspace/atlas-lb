package org.openstack.atlas.api.helpers;

import org.openstack.atlas.api.config.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.Configuration;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.*;

public final class ConfigurationHelper {
    public static boolean isAllowed(Configuration configuration, PublicApiServiceConfigurationKeys key) {
        //Verify the users request is allowed...
            return (configuration.hasKeys(key)
                    && configuration.getString(key).equals("true"));
    }
}
