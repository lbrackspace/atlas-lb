package org.openstack.atlas.logs.hadoop.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.joda.time.DateTime;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class StaticLogUtils {

    public static final Pattern zipLogPattern = Pattern.compile("^(.*)access_log_([0-9]+)_([0-9]{10}).zip$");

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

    public static List<Long> getHourKeysInRange(long beg, long end) {
        List<Long> hoursKeysL = new ArrayList<Long>();
        DateTime currDt = StaticDateTimeUtils.hourKeyToDateTime(beg, true);
        DateTime endDt = StaticDateTimeUtils.hourKeyToDateTime(end, true);
        while (true) {
            if (currDt.isAfter(endDt)) {
                break;
            }
            Long currHour = StaticDateTimeUtils.dateTimeToHourLong(currDt);
            hoursKeysL.add(currHour);
            currDt = currDt.plusHours(1);
        }
        return hoursKeysL;
    }
}
