package org.openstack.atlas.logs.hadoop.util;

import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openstack.atlas.exception.DateParseException;
import org.openstack.atlas.exception.StringParseException;
import org.openstack.atlas.logs.hadoop.writables.LogMapperOutputValue;
import org.openstack.atlas.util.staticutils.StaticStringUtils;

public final class LogChopper {

    private static final Log LOGGER = LogFactory.getLog(LogChopper.class);
    private static final Pattern LOG_PATTERN_1 = Pattern.compile("^(([^ ]++)\\s+([^ ]++)\\s+([^ ]++)\\s+([^ ]++)\\s+([^ ]++)\\s+\\[([^\\]]++)\\].*)$");
    private static final Pattern LOG_PATTERN_2 = Pattern.compile("^(([^ ]++)\\s+([^ ]++)\\s+([^ ]++) ([^ ]++)\\s+\\[([^\\]]++)\\].*)$");
    private static final Pattern LOG_PATTERN_3 = Pattern.compile("^(([^ ]++)\\s+([^ ]++)\\s+([^ ]++)\\s+\\[([^\\]]++)\\].*)$");
    private static final Pattern LOG_PATTERN_4 = Pattern.compile("^(([^ ]++) ([^ ]++) \\[([^\\]]++)\\].*)$");
    private static final Pattern LOG_PATTERN_5 = Pattern.compile("^(([^ ]++)\\s+\\[([^\\]]++)\\].*)$");
    private static final Pattern LOG_PATTERN_6 = Pattern.compile("^(([^ ]++)\\s+[^\\[]+\\[([^\\]]++)\\].*)$");

    private LogChopper() {
    }

    public static void getLogLineValues(String logLineIn, LogMapperOutputValue val) throws DateParseException, StringParseException {
        Matcher matcher;
        String date;
        String logLine;
        String lbName;
        logLine = StaticStringUtils.justOneCR(logLineIn);
        matcher = LOG_PATTERN_1.matcher(logLine);
        if (matcher.find()) {
            date = matcher.group(7);
            lbName = matcher.group(2);
            parseLogLine(logLine, lbName, true, date, null, val);
            return;
        }
        matcher = LOG_PATTERN_2.matcher(logLine);
        if (matcher.find()) {
            date = matcher.group(6);
            lbName = matcher.group(2);
            parseLogLine(logLine, lbName, true, date, null, val);
            return;
        }
        matcher = LOG_PATTERN_3.matcher(logLine);
        if (matcher.find()) {
            date = matcher.group(5);
            lbName = matcher.group(2);
            parseLogLine(logLine, lbName, true, date, null, val);
            return;
        }
        matcher = LOG_PATTERN_4.matcher(logLine);
        if (matcher.find()) {
            date = matcher.group(4);
            lbName = matcher.group(2);
            parseLogLine(logLine, lbName, true, date, null, val);
            return;
        }
        matcher = LOG_PATTERN_5.matcher(logLine);
        if (matcher.find()) {
            date = matcher.group(3);
            lbName = matcher.group(2);
            parseLogLine(logLine, lbName, true, date, null, val);
            return;
        }
        matcher = LOG_PATTERN_6.matcher(logLine);
        if (matcher.find()) {
            date = matcher.group(3);
            lbName = matcher.group(2);
            parseLogLine(logLine, lbName, true, date, null, val);
            return;
        }
        throw new StringParseException("Line did not match");

    }

    private static void parseLogLine(String logline, String loadBalancerName, boolean matchFound, String date, String sourceIp, LogMapperOutputValue val) throws DateParseException, StringParseException {
        String[] arr;
        int accountId;
        int loadBalancerId;
        String accountId_loadBalancerId;
        try {
            arr = loadBalancerName.split("_");
            accountId = Integer.parseInt(arr[0]);
            loadBalancerId = Integer.parseInt(arr[1]);
            accountId_loadBalancerId = accountId + "_" + loadBalancerId;
        } catch (Exception ex) {
            throw new StringParseException("Error could not decode accountId and loadbalancerId");
        }

        if (loadBalancerName.contains("_S")) {
            try {
                logline = stripSSL(logline, loadBalancerName, accountId_loadBalancerId);
            } catch (Exception ex) {
                throw new StringParseException("Unable to convert _S lb to standard lb log line");
            }
        }

        if (sourceIp == null) {
            sourceIp = "";
        }
        val.setAccountId(accountId);
        val.setSourceIp(sourceIp);
        val.setLoadbalancerName(accountId_loadBalancerId);
        val.setLoadbalancerId(loadBalancerId);
        val.setLogLine(StaticStringUtils.justOneCR(logline)); // Not sure why the carriage returns were stripped.
        org.joda.time.DateTime dt;
        try {
            dt = StaticDateTimeUtils.parseApacheDateTime(date, true);
        } catch (Exception ex) {
            throw new DateParseException("Coulden't parse date");
        }
        long dateOrd = StaticDateTimeUtils.dateTimeToOrdinalMillis(dt);
        val.setDate(dateOrd);
        return;

    }

    private static String stripSSL(String logline, String secureLoadBalancerName, String loadBalancerName) {
        String[] logSplit = logline.split(secureLoadBalancerName);
        String restOfLine = logSplit[1];

        return loadBalancerName + restOfLine;
    }
}
