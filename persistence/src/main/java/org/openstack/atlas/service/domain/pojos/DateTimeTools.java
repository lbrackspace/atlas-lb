package org.openstack.atlas.service.domain.pojos;

import java.util.Calendar;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class DateTimeTools {

    private static final Pattern YDM_RE = Pattern.compile("^([0-9]+)-([0-9]+)-([0-9]+)\\s*$");
    private static final Pattern YDMHM_RE = Pattern.compile("^([0-9]+)-([0-9]+)-([0-9]+)\\s+([0-9]+):([0-9]+)\\s*$");
    private static final Pattern YDMHMS_RE = Pattern.compile("^([0-9]+)-([0-9]+)-([0-9]+)\\s+([0-9]+):([0-9]+):([0-9]+)\\s*$");

    public static Calendar setCalendarAttrs(Calendar cal,Integer... iParams) {
        int pLength;
        pLength = iParams.length;

        if (pLength > 0 && iParams[0] != null) {cal.set(Calendar.YEAR, iParams[0]);}
        if (pLength > 1 && iParams[1] != null) {cal.set(Calendar.MONTH, iParams[1]-1);}
        if (pLength > 2 && iParams[2] != null) {cal.set(Calendar.DAY_OF_MONTH, iParams[2]);}
        if (pLength > 3 && iParams[3] != null) {cal.set(Calendar.HOUR_OF_DAY, iParams[3]);}
        if (pLength > 4 && iParams[4] != null) {cal.set(Calendar.MINUTE, iParams[4]);}
        if (pLength > 5 && iParams[5] != null) {cal.set(Calendar.SECOND, iParams[5]);}

        cal.set(Calendar.MILLISECOND, 0);

        return cal;
    }

    public static Calendar parseDate(String strIn) throws DateTimeToolException {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 0);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Matcher ydm_m = YDM_RE.matcher(strIn);
        Matcher ydmhm_m = YDMHM_RE.matcher(strIn);
        Matcher ydmhms_m = YDMHMS_RE.matcher(strIn);


        if (ydm_m.find()) {
            cal.set(Calendar.YEAR, Integer.parseInt(ydm_m.group(1)));
            cal.set(Calendar.MONTH, Integer.parseInt(ydm_m.group(2))-1);
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(ydm_m.group(3)));
            return cal;
        } else if (ydmhm_m.find()) {
            cal.set(Calendar.YEAR, Integer.parseInt(ydmhm_m.group(1)));
            cal.set(Calendar.MONTH, Integer.parseInt(ydmhm_m.group(2))-1);
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(ydmhm_m.group(3)));
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(ydmhm_m.group(4)));
            cal.set(Calendar.MINUTE, Integer.parseInt(ydmhm_m.group(5)));
            return cal;
        } else if (ydmhms_m.find()) {
            cal.set(Calendar.YEAR, Integer.parseInt(ydmhms_m.group(1)));
            cal.set(Calendar.MONTH, Integer.parseInt(ydmhms_m.group(2))-1);
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(ydmhms_m.group(3)));
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(ydmhms_m.group(4)));
            cal.set(Calendar.MINUTE, Integer.parseInt(ydmhms_m.group(5)));
            cal.set(Calendar.SECOND, Integer.parseInt(ydmhms_m.group(6)));
            return cal;
        } else {
            throw new DateTimeToolException("Error parsing String");
        }
    }

    public static String getDateYDM(Calendar cal) {
        int year;
        int month;
        int day;

        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);
        return String.format("%d-%d-%d", year, month+1, day);
    }

    public static String getDateYDMHM(Calendar cal) {
        int hour;
        int min;
        String out;
        hour = cal.get(Calendar.HOUR_OF_DAY);
        min = cal.get(Calendar.MINUTE);
        out = String.format("%s %d:%d", getDateYDM(cal), hour, min);
        return out;
    }

    public static String getDateYDMHMS(Calendar cal) {
        int secs;
        String out;
        secs = cal.get(Calendar.SECOND);
        out = String.format("%s:%d", getDateYDMHM(cal), secs);
        return out;
    }
}
