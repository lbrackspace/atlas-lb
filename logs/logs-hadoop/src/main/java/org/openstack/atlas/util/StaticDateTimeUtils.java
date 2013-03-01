package org.openstack.atlas.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class StaticDateTimeUtils {

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

    public static DateTime nowDateTime(boolean useUTC) {
        if (useUTC) {
            return DateTime.now().withZone(DateTimeZone.UTC);
        }
        return DateTime.now();
    }
}
