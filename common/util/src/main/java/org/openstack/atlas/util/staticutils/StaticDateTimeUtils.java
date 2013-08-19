package org.openstack.atlas.util.staticutils;

import java.util.Calendar;
import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class StaticDateTimeUtils {

    private static final long MILLIS_COEF = 10000000L;
    public static final DateTimeFormatter isoFormat = ISODateTimeFormat.dateTimeNoMillis();
    public static final DateTimeFormatter apacheDateTimeFormat = DateTimeFormat.forPattern("dd/MMM/yyyy:HH:mm:ss Z");
    public static final DateTimeFormatter utcApacheDateTimeFormat = apacheDateTimeFormat.withZone(DateTimeZone.UTC);
    public static final DateTimeFormatter sqlDateTimeFormat = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss");

    public static long dateTimeToOrdinalMillis(DateTime dt) {
        return dt.getYear() * 10000000000000L
                + dt.getMonthOfYear() * 100000000000L
                + dt.getDayOfMonth() * 1000000000L
                + dt.getHourOfDay() * 10000000L
                + dt.getMinuteOfHour() * 100000L
                + dt.getSecondOfMinute() * 1000L
                + dt.getMillisOfSecond();
    }

    public static DateTime OrdinalMillisToDateTime(long ord, boolean useUTC) {
        int millis = (int) (ord % 1000);
        ord /= 1000;
        int secs = (int) (ord % 100);
        ord /= 100;
        int mins = (int) (ord % 100);
        ord /= 100;
        int hours = (int) (ord % 100);
        ord /= 100;
        int days = (int) (ord % 100);
        ord /= 100;
        int months = (int) (ord % 100);
        ord /= 100;
        int years = (int) (ord % 10000);

        if (useUTC) {
            return new DateTime(years, months, days, hours, mins, secs, millis, DateTimeZone.UTC);
        }
        return new DateTime(years, months, days, hours, mins, secs, millis);
    }

    public static String toApacheDateTime(DateTime dt) {
        return apacheDateTimeFormat.print(dt);
    }

    public static DateTime parseApacheDateTime(String apacheDateStr, boolean useUTC) {
        if (useUTC) {
            utcApacheDateTimeFormat.parseDateTime(apacheDateStr);
        }
        return apacheDateTimeFormat.parseDateTime(apacheDateStr);

    }

    public static DateTime nowDateTime(boolean useUTC) {
        if (useUTC) {
            return DateTime.now().withZone(DateTimeZone.UTC);
        }
        return DateTime.now();
    }

    public static String toSqlTime(Date date) {
        return sqlDateTimeFormat.print(toDateTime(date, true));
    }

    public static double getEpochSeconds() {
        return ((double) System.currentTimeMillis()) * 0.001;
    }

    public static DateTime toDateTime(Date date, boolean useUTC) {
        if (useUTC) {
            return new DateTime(date).withZone(DateTimeZone.UTC);
        }
        return new DateTime(date);
    }

    public static Date toDate(Calendar cal) {
        return cal.getTime();
    }

    public static Date toDate(DateTime dt) {
        return dt.toDate();
    }

    public static DateTime toDateTime(Calendar cal, boolean useUTC) {
        if (useUTC) {
            return new DateTime(cal).withZone(DateTimeZone.UTC);
        }
        return new DateTime(cal);
    }

    public static DateTime hourKeyToDateTime(String dateHour, boolean useUTC) {
        return hourKeyToDateTime(Long.parseLong(dateHour), useUTC);
    }

    public static DateTime hourKeyToDateTime(long ord, boolean useUTC) {
        return StaticDateTimeUtils.OrdinalMillisToDateTime(ord * MILLIS_COEF, useUTC);
    }

    public static long dateTimeToHourLong(DateTime dt) {
        return StaticDateTimeUtils.dateTimeToOrdinalMillis(dt) / MILLIS_COEF;
    }
}
