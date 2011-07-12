package org.openstack.atlas.util;

import org.openstack.atlas.io.LbLogsWritable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LogChopper {

    private static final Log LOGGER = LogFactory.getLog(LogChopper.class);

    private static final Pattern HTTP_LB_LOG_PATTERN = Pattern.compile("^((\\S+) (\\S+) (\\S+) (\\S+) (\\S+) \\[(\\d+\\/\\w+\\/\\d+:\\d+:\\d+:\\d+) \\S+\\] \"(\\S+ \\S.*)(HTTP\\/1\\.\\d*)\" (\\S+) (\\d+) \"(.*)\" \"(.*)\")$");
    private static final Pattern NON_HTTP_LB_LOG_PATTERN = Pattern.compile("^((\\S+) \\[(\\d+\\/\\w+\\/\\d+:\\d+:\\d+:\\d+) \\S+\\] (\\S+) (\\S+) (\\S+) (\\S+) (\\S+) (\\S+))$");

    private LogChopper() {
    }

    public static LbLogsWritable getLbLogStats(String logline) throws Exception {
        Matcher matcher = HTTP_LB_LOG_PATTERN.matcher(logline);
        boolean matchFound = matcher.find();
        if (matchFound) {
            String[] arr = matcher.group(2).split("_");
            int accountId = Integer.parseInt(arr[0]);
            int loadBalancerId = Integer.parseInt(arr[1]);
            return new LbLogsWritable(accountId,
                    matcher.group(4),
                    accountId + "_" + loadBalancerId,
                    loadBalancerId,
                    new DateTime(matcher.group(7), DateTime.APACHE).getCalendar(),
                    logline);
        } else {
            matcher = NON_HTTP_LB_LOG_PATTERN.matcher(logline);
            matchFound = matcher.find();
            if (matchFound) {
                 String[] arr = matcher.group(2).split("_");
                int accountId = Integer.parseInt(arr[0]);
                int loadBalancerId = Integer.parseInt(arr[1]);
                
                return new LbLogsWritable(accountId,
                        "",
                        accountId + "_" + loadBalancerId,
                        loadBalancerId,
                        new DateTime(matcher.group(3), DateTime.APACHE).getCalendar(),
                        logline
                        );
            } else {
                LOGGER.error(logline);
                throw getLoglineError(logline.toString());

            }
        }
    }

    private static Exception getLoglineError(String logline) {
        return new Exception("TODO Figure out what happens on bad log lines\n" + logline);
    }
}
