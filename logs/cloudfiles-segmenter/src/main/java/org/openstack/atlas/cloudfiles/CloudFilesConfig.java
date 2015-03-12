package org.openstack.atlas.cloudfiles;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openstack.atlas.config.HadoopLogsConfigs;
import org.openstack.atlas.config.LbLogsConfiguration;
import org.openstack.atlas.config.LbLogsConfigurationKeys;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class CloudFilesConfig {

    protected static String authEndpoint;
    protected static String user;
    protected static String apiKey;
    protected static String filesEndpoint;
    protected static String filesAccount;
    protected static int daysLzo;
    protected static int segmentSize;
    protected static int resetCount = 0;

    static {
        resetConfig();
    }

    public static void resetConfig() {
        resetConfig(null);
    }

    public static String staticToString() {
        return "CloudFilesConfig{\ncloud_files_lzo_auth_api_endpoint=" + authEndpoint
                + "\ncloud_files_lzo_user=" + user
                + "\ncloud_files_lzo_key=" + apiKey
                + "\ncloud_files_lzo_storage_api_endpoint=" + filesEndpoint
                + "\ncloud_files_lzo_accoun=" + filesAccount
                + "\ncloud_files_lzo_segment_size=" + segmentSize
                + "\ncloud_files_days_of_lzos_to_keep=" + daysLzo
                + "\n}";
    }

    public static void resetConfig(String filePath) {
        LbLogsConfiguration lbLogsConf;
        if (filePath == null) {
            lbLogsConf = new LbLogsConfiguration();
        } else {
            lbLogsConf = new LbLogsConfiguration(StaticFileUtils.expandUser(filePath));
        }
        authEndpoint = lbLogsConf.getString(LbLogsConfigurationKeys.cloud_files_lzo_auth_api_endpoint);
        apiKey = lbLogsConf.getString(LbLogsConfigurationKeys.cloud_files_lzo_key);
        user = lbLogsConf.getString(LbLogsConfigurationKeys.cloud_files_lzo_user);
        segmentSize = getInt(lbLogsConf, LbLogsConfigurationKeys.cloud_files_lzo_segment_size);
        filesAccount = lbLogsConf.getString(LbLogsConfigurationKeys.cloud_files_lzo_account);
        filesEndpoint = lbLogsConf.getString(LbLogsConfigurationKeys.cloud_files_lzo_storage_api_endpoint);
        daysLzo = getInt(lbLogsConf, LbLogsConfigurationKeys.cloud_files_days_of_lzos_to_keep);
        resetCount++;
    }

    public static int getResetCount() {
        return resetCount;
    }

    public static String getUser() {
        return user;
    }

    public static String getApiKey() {
        return apiKey;
    }

    public static String getAuthEndpoint() {
        return authEndpoint;
    }

    public static String getFilesEndpoint() {
        return filesEndpoint;
    }

    public static String getFilesAccount() {
        return filesAccount;
    }

    public static int getSegmentSize() {
        return segmentSize;
    }

    private static int getInt(LbLogsConfiguration lbConf, LbLogsConfigurationKeys key) {
        return Integer.parseInt(lbConf.getString(key));
    }

    public static int getDaysLzo() {
        return daysLzo;
    }
}
