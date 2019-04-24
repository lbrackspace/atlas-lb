package org.openstack.atlas.api.helpers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateHelpers {
    private static final String DATE_FORMAT = "yyyy-MM-dd:HH:mm:ss";

    public static String getDate(Date date) {
        String startDate;
        //20110215-130916
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        startDate = df.format(date);

        return startDate;
    }

    public static String getTotalTimeTaken(long time) {
        String timeTaken = "";

        long now = Calendar.getInstance().getTimeInMillis();
        long diff = now - time;
        timeTaken = Long.toString(diff);
        return timeTaken;
    }

    /**
     * Prepares url query parameter with date/time strings.
     * @param startTimeParam start time string.
     * @param endTimeParam end time string.
     * @return
     */
    public static String prepareDateParameterString(String startTimeParam, String endTimeParam) {
        if (startTimeParam != null && endTimeParam != null){
            return String.format("startTime=%s&endTime=%s&", startTimeParam, endTimeParam);
        } else if (startTimeParam != null){
            return String.format("startTime=%s&", startTimeParam);
        } else if (endTimeParam != null){
            return String.format("endTime=%s&", endTimeParam);
        }
        return org.apache.commons.lang3.StringUtils.EMPTY;
    }
}
