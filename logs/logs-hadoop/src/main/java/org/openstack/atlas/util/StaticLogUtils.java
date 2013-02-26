package org.openstack.atlas.util;

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
}
