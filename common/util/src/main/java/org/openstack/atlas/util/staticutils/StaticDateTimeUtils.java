package org.openstack.atlas.util.staticutils;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
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

    public static DateTime iso8601ToDateTime(String iso8601Str) {
        DateTime dt = DateTime.parse(iso8601Str, ISODateTimeFormat.dateTimeParser().withZone(DateTimeZone.UTC));
        return dt;
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

    public static Calendar toCal(DateTime dt) {
        return dt.toCalendar(Locale.getDefault());
    }

    public static int DateTimeToHourKeyInt(DateTime dt) {
        return dt.getHourOfDay() * 1
                + dt.getDayOfMonth() * 100
                + dt.getMonthOfYear() * 10000
                + dt.getYear() * 1000000;
    }

    public static int dateTimeToHourKeyBinInt(DateTime dt) {
        return ((dt.getYear() & 0xf000) << 19)
                | ((dt.getMonthOfYear() & 0xf0) << 15)
                | ((dt.getDayOfMonth() & 0x1f) << 10)
                | ((dt.getYear() & 0x1f) << 5);

    }

    public static DateTime hourKeyIntBinToDateTime(int hourKeyBinInt, boolean useUTC) {
        int year = 0xf000 & (hourKeyBinInt >> 19);
        int month = 0xf0 & (hourKeyBinInt >> 15);
        int day = 0x1f & (hourKeyBinInt >> 10);
        int hour = 0x1f & (hourKeyBinInt >> 5);
        if (useUTC) {
            return new DateTime(year, month, day, hour, 0, 0, 0, DateTimeZone.UTC);
        } else {
            return new DateTime(year, month, day, hour, 0, 0, 0);
        }
    }

    public static double secondsBetween(DateTime before, DateTime after){
        Duration duration = new Duration(before, after);
        double seconds = duration.getMillis()*0.001;
        return seconds;
    }

    public static int getNextHourKeyInt(int hourKey) {
        long hourKeyLong = (long) hourKey * 10000000L;
        DateTime dt = OrdinalMillisToDateTime(hourKeyLong, true).plusHours(1);
        return DateTimeToHourKeyInt(dt);
    }
}
