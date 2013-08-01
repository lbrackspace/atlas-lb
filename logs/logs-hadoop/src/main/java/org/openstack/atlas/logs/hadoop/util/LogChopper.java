package org.openstack.atlas.logs.hadoop.util;

import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;
import org.openstack.atlas.io.LbLogsWritable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openstack.atlas.exception.DateParseException;
import org.openstack.atlas.exception.StringParseException;
import org.openstack.atlas.logs.hadoop.writables.LogMapperOutputValue;

public final class LogChopper {

    private static final Log LOGGER = LogFactory.getLog(LogChopper.class);
    private static final Pattern HTTP_LB_LOG_PATTERN = Pattern.compile("^(([^ ]++) ([^ ]++) ([^ ]++) ([^ ]++) ([^ ]++) \\[([^\\]]++)\\] \"([^ ]++ \\S.*)(HTTP\\/1\\.\\d*)\" ([^ ]++) (\\d+) \"(.*)\" \"(.*)\")$");
    private static final Pattern HTTP_LB_LOG_PATTERN_IP = Pattern.compile("^(([^ ]++) ([^ ]++) ([^ ]++) ([^ ]++) ([^ ]++) \\[([^\\]]++)\\] \"([^ ]++ \\S.*)(HTTP\\/1\\.\\d*)\" ([^ ]++) (\\d+) \"(.*)\" \"(.*)\" ([^ ]++))$");
    private static final Pattern NON_HTTP_LB_LOG_PATTERN = Pattern.compile("^(([^ ]++) \\[([^\\]]++)\\] ([^ ]++) ([^ ]++) ([^ ]++) ([^ ]++) ([^ ]++) ([^ ]++))$");

    private LogChopper() {
    }

    public static void getLogLineValues(String logLine, LogMapperOutputValue val) throws DateParseException, StringParseException {

        Matcher matcher = HTTP_LB_LOG_PATTERN.matcher(logLine);
        Matcher matcherIP = HTTP_LB_LOG_PATTERN_IP.matcher(logLine);
        boolean matchFound = matcher.find();
        String date;

        if (matchFound) {
            date = matcher.group(7);
            String sourceIp = matcher.group(4);
            parseLogLine(logLine, matcher, matchFound, date, sourceIp, val);
            return;
        } else if (matcherIP.find()) {
            matchFound = true;
            date = matcherIP.group(7);
            String sourceIp = matcherIP.group(4);
            parseLogLine(logLine, matcherIP, matchFound, date, sourceIp, val);
            return;
        } else {
            matcher = NON_HTTP_LB_LOG_PATTERN.matcher(logLine);
            matchFound = matcher.find();
            if (!matchFound) {
                throw new StringParseException("Date did not parse on Non HTTP LoadBalancer");
            }
            date = matcher.group(3);
            parseLogLine(logLine, matcher, matchFound, date, null, val);
            return;
        }
    }

    @Deprecated
    public static LbLogsWritable getLbLogStats(String logline) throws StringParseException {
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

    @Deprecated
    private static LbLogsWritable parseLogLine(String logline, Matcher matcher, boolean matchFound, String date, String sourceIp) throws StringParseException {
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
                    StaticDateTimeUtils.parseApacheDateTime(date, false).toGregorianCalendar(),
                    logline);
        } else {
            LOGGER.error(logline);
            throw new StringParseException("Line did not match");

        }
    }

    private static void parseLogLine(String logline, Matcher matcher, boolean matchFound, String date, String sourceIp, LogMapperOutputValue val) throws DateParseException, StringParseException {
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
            val.setAccountId(accountId);
            val.setSourceIp(sourceIp);
            val.setLoadbalancerName(accountId_loadBalancerId);
            val.setLoadbalancerId(loadBalancerId);
            org.joda.time.DateTime dt;
            try {
                dt = StaticDateTimeUtils.parseApacheDateTime(date, true);
            } catch (Exception ex) {
                throw new DateParseException("Coulden't parse date");
            }
            long dateOrd = StaticDateTimeUtils.dateTimeToOrdinalMillis(dt);
            val.setDate(dateOrd);
            val.setLogLine(logline + "\n"); // Not sure why the carriage returns were stripped.
            return;
        } else {
            LOGGER.error(logline);
            throw new StringParseException("Line did not match");
        }
    }

    private static String stripSSL(String logline, String secureLoadBalancerName, String loadBalancerName) {
        String[] logSplit = logline.split(secureLoadBalancerName);
        String restOfLine = logSplit[1];

        return loadBalancerName + restOfLine;
    }
}
