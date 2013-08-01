package org.openstack.atlas.logs.hadoop.util;

import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class LogFileNameBuilder {

    public static String getContainerName(String lbId, String lbName, String dateString) {
        String monthYear = StaticFileUtils.getMonthYearFromFileDate(dateString);
        StringBuilder sb = new StringBuilder();
        sb.append("lb ");
        sb.append(lbId).append(" ");
        sb.append(lbName).append(" ");
        sb.append(monthYear);
        return getFormattedName(sb.toString());
    }

    public static String getRemoteFileName(String lbId, String lbName, String dateString) {
        StringBuilder sb = new StringBuilder();
        sb.append("lb ");
        sb.append(lbId).append(" ");
        sb.append(lbName).append(" ");
        sb.append(getFormattedFileDate(dateString));
        sb.append(".zip");
        return getFormattedName(sb.toString());
    }

    public static String getFormattedName(String name) {
        return name.replaceAll(" ", "_").replaceAll("/", "_");

    }

    public static String getFormattedFileDate(String fileDate) {
        char[] c = fileDate.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < c.length; i++) {
            sb.append(c[i]);
            if (i == 3 || i == 5 || i == 7) {
                sb.append("-");
            }
        }
        sb.append(":00");
        return sb.toString();
    }

    public static String getZipFileName(int loadbalancerId, int fileHour) {
        return "access_log_" + loadbalancerId + "_" + fileHour + ".zip";
    }

    public static String getZipContentsName(int loadbalancerId, int fileHour) {
        return "access_log_" + loadbalancerId + "_" + fileHour;
    }
}
