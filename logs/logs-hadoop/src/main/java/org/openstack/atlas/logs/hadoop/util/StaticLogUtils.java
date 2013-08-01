package org.openstack.atlas.logs.hadoop.util;

import java.io.File;
import java.io.IOException;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class StaticLogUtils {

    /**
     * /var/log/zxtm/hadoop/cache/2012021005/1/access_log_10_2012021005.zip => 1
     */
    public static String getAccountId(String absoluteFileName) {
        String accountDirectory = absoluteFileName.substring(0, absoluteFileName.lastIndexOf("/"));
        String accountId = accountDirectory.substring(accountDirectory.lastIndexOf("/") + 1, accountDirectory.length());
        return accountId;
    }

    /**
     * /var/log/zxtm/hadoop/cache/2012021005/1/access_log_10_2012021005.zip => 10
     */
    public static String getLoadBalancerId(String absoluteFileName) {
        return absoluteFileName.split("_")[2];
    }

    public static boolean isSymLink(String filePath) throws IOException {
        File file = new File(StaticFileUtils.expandUser(filePath));
        return org.apache.commons.io.FileUtils.isSymlink(file);
    }

    public static String getZipFileName(int loadbalancerId, int fileHour) {
        return "access_log_" + loadbalancerId + "_" + fileHour + ".zip";
    }

    public static String getZipContentsName(int loadbalancerId, int fileHour) {
        return "access_log_" + loadbalancerId + "_" + fileHour;
    }
}
