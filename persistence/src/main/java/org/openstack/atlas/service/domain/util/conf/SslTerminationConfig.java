package org.openstack.atlas.service.domain.util.conf;

import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class SslTerminationConfig {

    private static int resetCount;
    private static String encryptKey;

    static {
        resetCount = 0;
        resetConfigs(null);
    }

    public static int resetConfigs(String filePath) {
        synchronized (SslTerminationConfig.class) {
            resetCount++;
        }
        RestApiConfiguration conf;
        if (filePath == null) {
            conf = new RestApiConfiguration();
        } else {
            conf = new RestApiConfiguration(StaticFileUtils.expandUser(filePath));
        }
        String key = conf.getString(PublicApiServiceConfigurationKeys.ssl_private_encrypt_key);
        synchronized (SslTerminationConfig.class) {
            encryptKey = key;
        }
        return resetCount;
    }

    public static int getResetCount() {
        return resetCount;
    }

    public static String getEncryptKey() {
        return encryptKey;
    }
}
