package org.openstack.atlas.util.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CalendarUtils {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static Calendar stripOutMinsAndSecs(Calendar cal) {
        Calendar newCal = Calendar.getInstance();
        newCal.setTime(cal.getTime());
        newCal.set(Calendar.MINUTE, 0);
        newCal.set(Calendar.SECOND, 0);
        newCal.set(Calendar.MILLISECOND, 0);
        return newCal;
    }

    public static Calendar stringToCalendar(String calAsString) throws ParseException {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        cal.setTime(sdf.parse(calAsString));
        return cal;
    }

    public static Calendar copy(Calendar calToCopy) {
        Calendar copy = Calendar.getInstance();
        copy.setTime(calToCopy.getTime());
        return copy;
    }

    public static String calendarToString(Calendar cal) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(cal.getTime());
    }

    public static boolean isTopOfTheHour(Calendar cal) {
        return cal.get(Calendar.MINUTE) == 0 && cal.get(Calendar.SECOND) == 0 && cal.get(Calendar.MILLISECOND) == 0;
    }

    /*
     *  This method returns true if cal is between startTime (inclusive) and endTime (inclusive or exclusive).
     */
    public static boolean isBetween(Calendar cal, Calendar startTime, Calendar endTime, boolean endTimeInclusive) {
        if (endTimeInclusive) {
            return cal.compareTo(startTime) >= 0 && cal.compareTo(endTime) <= 0;
        }

        return cal.compareTo(startTime) >= 0 && cal.compareTo(endTime) < 0;
    }

    public static Duration calcDuration(Calendar startTime, Calendar endTime) {
        final int MILLI_TO_SEC_CONVERSION = 1000;
        final int SEC_TO_MIN_CONVERSION = 60;
        final int MIN_TO_HOUR_CONVERSION = 60;

        Duration duration = new Duration();
        long elapsedTimeInMillis = endTime.getTimeInMillis() - startTime.getTimeInMillis();
        long elapsedTimeInSecs = elapsedTimeInMillis / MILLI_TO_SEC_CONVERSION;
        long elapsedTimeInMins = elapsedTimeInSecs / SEC_TO_MIN_CONVERSION;
        long elapsedTimeInHours = elapsedTimeInMins / MIN_TO_HOUR_CONVERSION;

        duration.setHours(elapsedTimeInHours);
        duration.setMins(elapsedTimeInMins - elapsedTimeInHours * MIN_TO_HOUR_CONVERSION);
        duration.setSecs(elapsedTimeInSecs - elapsedTimeInMins * SEC_TO_MIN_CONVERSION);

        return duration;
    }
}