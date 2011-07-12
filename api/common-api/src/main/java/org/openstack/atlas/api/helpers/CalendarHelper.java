package org.openstack.atlas.api.helpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public final class CalendarHelper {

    public static Calendar generateCalendar(String dateAsString) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return generateCalendar(dateAsString, sdf);
    }

    public static Calendar generateCalendar(String dateAsString, SimpleDateFormat simpleDateFormat) throws ParseException {
        Date date = simpleDateFormat.parse(dateAsString);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public static Calendar zeroOutTime(Calendar cal) {
        Calendar newCal = Calendar.getInstance();
        newCal.setTime(cal.getTime());
        newCal.set(Calendar.HOUR_OF_DAY, 0);
        newCal.set(Calendar.MINUTE, 0);
        newCal.set(Calendar.SECOND, 0);
        newCal.set(Calendar.MILLISECOND, 0);
        return newCal;
    }

    public static Calendar maxOutTime(Calendar cal) {
        Calendar newCal = Calendar.getInstance();
        newCal.setTime(cal.getTime());
        newCal.set(Calendar.HOUR_OF_DAY, 23);
        newCal.set(Calendar.MINUTE, 59);
        newCal.set(Calendar.SECOND, 59);
        newCal.set(Calendar.MILLISECOND, 999);
        return newCal;
    }

    public static Calendar getProperUsageStartDate(String startDateParam, Calendar today) throws ParseException {
        Calendar startDate;
        if (startDateParam == null || startDateParam.isEmpty()) {
            startDate = new GregorianCalendar();
            startDate.setTime(today.getTime());
            startDate.add(Calendar.DATE, -90);
        } else {
            startDate = CalendarHelper.generateCalendar(startDateParam);
        }
//        startDate = CalendarHelper.zeroOutTime(startDate);
        return startDate;
    }

    public static Calendar getProperUsageEndDate(String endDateParam, Calendar today) throws ParseException {
        Calendar endDate;
        if (endDateParam == null || endDateParam.isEmpty()) {
            endDate = new GregorianCalendar();
            endDate.setTime(today.getTime());
        } else {
            endDate = CalendarHelper.generateCalendar(endDateParam);
        }
//        endDate = CalendarHelper.maxOutTime(endDate);
        return endDate;
    }
}
