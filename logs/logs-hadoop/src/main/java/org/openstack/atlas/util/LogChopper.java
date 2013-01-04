package org.openstack.atlas.util;

import org.openstack.atlas.io.LbLogsWritable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LogChopper {

    private static final Log LOGGER = LogFactory.getLog(LogChopper.class);

    private static final Pattern HTTP_LB_LOG_PATTERN = Pattern.compile("^((\\S+) (\\S+) (\\S+) (\\S+) (\\S+) \\[(\\d+\\/\\w+\\/\\d+:\\d+:\\d+:\\d+) \\S+\\] \"(\\S+ \\S.*)(HTTP\\/1\\.\\d*)\" (\\S+) (\\d+) \"(.*)\" \"(.*)\")$");
    private static final Pattern HTTP_LB_LOG_PATTERN_IP = Pattern.compile("^((\\S+) (\\S+) (\\S+) (\\S+) (\\S+) \\[(\\d+\\/\\w+\\/\\d+:\\d+:\\d+:\\d+) \\S+\\] \"(\\S+ \\S.*)(HTTP\\/1\\.\\d*)\" (\\S+) (\\d+) \"(.*)\" \"(.*)\" (\\S+))$");
    private static final Pattern NON_HTTP_LB_LOG_PATTERN = Pattern.compile("^((\\S+) \\[(\\d+\\/\\w+\\/\\d+:\\d+:\\d+:\\d+) \\S+\\] (\\S+) (\\S+) (\\S+) (\\S+) (\\S+) (\\S+))$");

    private LogChopper() {
    }

    public static LbLogsWritable getLbLogStats(String logline) throws Exception {
        Matcher matcher = HTTP_LB_LOG_PATTERN.matcher(logline);
        Matcher matcherIP = HTTP_LB_LOG_PATTERN_IP.matcher(logline);
        boolean matchFound = matcher.find();
        String date;

        if (matchFound) {
            date = matcher.group(7);
            String sourceIp = matcher.group(4);
            return parseLogLine(logline, matcher, matchFound, date, sourceIp);
        } else if (matcherIP.find()) {
            matchFound = true;
            date = matcherIP.group(7);
            String sourceIp = matcherIP.group(4);
            return parseLogLine(logline, matcherIP, matchFound, date, sourceIp);
        } else {
            matcher = NON_HTTP_LB_LOG_PATTERN.matcher(logline);
            matchFound = matcher.find();
            date = matcher.group(3);
            return parseLogLine(logline, matcher, matchFound, date, null);
        }
    }

    private static LbLogsWritable parseLogLine(String logline, Matcher matcher, boolean matchFound, String date, String sourceIp) throws Exception {
        if (matchFound) {
            String loadBalancerName = matcher.group(2);
            String[] arr = loadBalancerName.split("_");
            int accountId = Integer.parseInt(arr[0]);
            int loadBalancerId = Integer.parseInt(arr[1]);
            String accountId_loadBalancerId = accountId + "_" + loadBalancerId;


            if (loadBalancerName.contains("_S")) {
                logline = stripSSL(logline, loadBalancerName, accountId_loadBalancerId);
            }

            if (sourceIp == null) {
                sourceIp = "";
            }
            return new LbLogsWritable(accountId,
                    sourceIp,
                    accountId_loadBalancerId,
                    loadBalancerId,
                    new DateTime(date, DateTime.APACHE).getCalendar(),
                    logline
            );
        } else {
            LOGGER.error(logline);
            throw getLoglineError(logline.toString());

        }
    }

    private static String stripSSL(String logline, String secureLoadBalancerName, String loadBalancerName) {
        String[] logSplit = logline.split(secureLoadBalancerName);
        String restOfLine = logSplit[1];

        return loadBalancerName + restOfLine;
    }

    private static Exception getLoglineError(String logline) {
        return new Exception("TODO Figure out what happens on bad log lines\n" + logline);
    }
}
