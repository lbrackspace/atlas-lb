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
}
