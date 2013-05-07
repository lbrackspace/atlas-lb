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
}