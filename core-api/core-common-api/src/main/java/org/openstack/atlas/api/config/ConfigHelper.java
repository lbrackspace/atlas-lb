package org.openstack.atlas.api.config;

import org.openstack.atlas.common.config.Configuration;

import java.util.ArrayList;
import java.util.List;

public class ConfigHelper {

    public static List<String> getPluginsFromConfiguration() {
        String enabledAdapter = getAdapterFromConfiguration();
        List<String> enabledExtensions = getExtensionPrefixesFromConfiguration();
        List<String> enabledPlugins = new ArrayList<String>();

        if (enabledAdapter != null) {
            enabledPlugins.add(enabledAdapter + "-adapter");
        }

        if (!enabledExtensions.isEmpty()) {
            for (String enabledExtension : enabledExtensions) {
                enabledPlugins.add(enabledExtension + "-persistence");
                enabledPlugins.add(enabledExtension + "-api");
                enabledPlugins.add(enabledExtension + "-datamodel");
            }
        } else {
            enabledPlugins.add("core-root");
        }

        return enabledPlugins;
    }

    public static String getAdapterFromConfiguration() {
        Configuration configuration = new RestApiConfiguration();
        return configuration.getString(PublicApiServiceConfigurationKeys.adapter);
    }

    public static List<String> getExtensionPrefixesFromConfiguration() {
        List<String> enabledExtensions = new ArrayList<String>();
        Configuration configuration = new RestApiConfiguration();
        String extensions = configuration.getString(PublicApiServiceConfigurationKeys.extensions);

        if (extensions == null || extensions.equals("")) return enabledExtensions;

        final String[] split = extensions.split(",");
        for (String s : split) {
            enabledExtensions.add(s.trim().toLowerCase());
        }

        return enabledExtensions;
    }
}
