package org.openstack.atlas.util.staticutils;



import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import static java.time.ZoneOffset.UTC;


public class StaticDateTimeUtils {

    private static final long MILLIS_COEF = 10000000L;
    public static final DateTimeFormatter apacheDateTimeFormat = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");
    public static final DateTimeFormatter utcApacheDateTimeFormat = apacheDateTimeFormat.withZone(UTC);

    public static long dateTimeToOrdinalMillis(ZonedDateTime dt) {
        return dt.getYear() * 10000000000000L
                + dt.getMonthValue() * 100000000000L
                + dt.getDayOfMonth() * 1000000000L
                + dt.getHour() * 10000000L
                + dt.getMinute() * 100000L
                + dt.getSecond() * 1000L
                + dt.getNano();
    }

    /**
     * method to convert OrdinalMillis to date time
     * @param ord
     * @param useUTC
     * @return
     */
    public static ZonedDateTime OrdinalMillisToDateTime(long ord, boolean useUTC) {
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
            return  ZonedDateTime.of(years, months, days, hours, mins, secs, millis, UTC);
        }
        return  ZonedDateTime.of(years, months, days, hours, mins, secs, millis,TimeZone.getDefault().toZoneId());
    }

    /**
     * method to convert ZonedDateTime to string
     * @param dt
     * @return String
     */
    public static String toApacheDateTime(ZonedDateTime dt) {
        return apacheDateTimeFormat.format(dt);
    }

    /**
     * method to convert string to ZonedDateTime
     * @param apacheDateStr
     * @param useUTC
     * @return ZonedDateTime
     */
    public static ZonedDateTime parseApacheDateTime(String apacheDateStr, boolean useUTC) {
        if (useUTC) {
            utcApacheDateTimeFormat.parse(apacheDateStr);
        }
        return (ZonedDateTime) apacheDateTimeFormat.parse(apacheDateStr);

    }

    /**
     *
     * @param useUTC
     * @return
     */
    public static ZonedDateTime nowDateTime(boolean useUTC) {
        if (useUTC) {
            return ZonedDateTime.now().withZoneSameLocal(UTC);
        }
        return ZonedDateTime.now();
    }

    /**
     * method to convert LocalDate to String
     * @param date
     * @return
     */
    public static String toSqlTime(LocalDate date) {
        return toDateTime(date, true).toString();
    }

    /**
     * method to convert current time in double
     * @return double
     */
    public static double getEpochSeconds() {
        return ((double) System.currentTimeMillis()) * 0.001;
    }

    /**
     * method to convert LocalDate to ZonedDateTime
     * @param date
     * @param useUTC
     * @return ZonedDateTime
     */
    public static ZonedDateTime toDateTime(LocalDate date, boolean useUTC) {
        if (useUTC) {
            return  ZonedDateTime.of(date, LocalTime.ofSecondOfDay(0),UTC);
        }
        return  ZonedDateTime.of(date,LocalTime.ofSecondOfDay(0), TimeZone.getDefault().toZoneId());
    }

    /**
     * method to convert Calendar to Date
     * @param cal
     * @return Date
     */
    public static Date toDate(Calendar cal) {
        return cal.getTime();
    }

    /**
     * method to convert ZonedDateTime to Date
     * @param dt
     * @return
     */
    public static Date toDate(ZonedDateTime dt) {
        return java.util.Date.from(dt.toInstant());
    }

    /**
     * method to convert Calendar to ZonedDateTime
     * @param cal
     * @param useUTC
     * @return ZonedDateTime
     */
    public static ZonedDateTime toDateTime(Calendar cal, boolean useUTC) {
        if (useUTC) {
            return  ZonedDateTime.of(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH),cal.get(Calendar.HOUR),cal.get(Calendar.MINUTE),cal.get(Calendar.SECOND), cal.get(Calendar.MILLISECOND)*1000000,UTC).withZoneSameLocal(UTC);
        }
        return ZonedDateTime.of(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH),cal.get(Calendar.HOUR),cal.get(Calendar.MINUTE),cal.get(Calendar.SECOND), cal.get(Calendar.MILLISECOND)*1000000,TimeZone.getDefault().toZoneId());
    }

    /**
     * method to convert String to ZonedDateTime
     * @param dateHour
     * @param useUTC
     * @return
     */
    public static ZonedDateTime hourKeyToDateTime(String dateHour, boolean useUTC) {
        return hourKeyToDateTime(Long.parseLong(dateHour), useUTC);
    }

    /**
     * method to convert long to ZonedDateTime
     * @param ord
     * @param useUTC
     * @return
     */
    public static ZonedDateTime hourKeyToDateTime(long ord, boolean useUTC) {
        return StaticDateTimeUtils.OrdinalMillisToDateTime(ord * MILLIS_COEF, useUTC);
    }

    /**
     * method to convert ZonedDateTime to long
     * @param dt
     * @return
     */
    public static long dateTimeToHourLong(ZonedDateTime dt) {
        return StaticDateTimeUtils.dateTimeToOrdinalMillis(dt) / MILLIS_COEF;
    }

    /**
     * method to convert ZonedDateTime to Calendar
     * @param dt
     * @return Calendar
     */
    public static Calendar toCal(ZonedDateTime dt) {
        return GregorianCalendar.from(dt);
    }

    /**
     * method to convert ZonedDateTime to int
     * @param dt
     * @return int
     */
    public static int DateTimeToHourKeyInt(ZonedDateTime dt) {
        return dt.getHour() * 1
                + dt.getDayOfMonth() * 100
                + dt.getMonthValue() * 10000
                + dt.getYear() * 1000000;
    }

    /**
     *method to convert ZonedDateTime value into int
     * @param dt
     * @return
     */
    public static int dateTimeToHourKeyBinInt(ZonedDateTime dt) {
        return ((dt.getYear() & 0xf000) << 19)
                | ((dt.getMonthValue() & 0xf0) << 15)
                | ((dt.getDayOfMonth() & 0x1f) << 10)
                | ((dt.getYear() & 0x1f) << 5);

    }

    /**
     *method to convert hours to ZonedDateTime
     * @param hourKeyBinInt
     * @param useUTC
     * @return
     */
    public static ZonedDateTime hourKeyIntBinToDateTime(int hourKeyBinInt, boolean useUTC) {
        int year = 0xf000 & (hourKeyBinInt >> 19);
        int month = 0xf0 & (hourKeyBinInt >> 15);
        int day = 0x1f & (hourKeyBinInt >> 10);
        int hour = 0x1f & (hourKeyBinInt >> 5);
        if (useUTC) {
            return  ZonedDateTime.of(year, month, day, hour, 0, 0, 0, UTC);
        } else {
            return  ZonedDateTime.of(year, month, day, hour, 0, 0, 0,TimeZone.getDefault().toZoneId());
        }
    }

    /**
     * method to return seconds between before date and after date
     * @param before
     * @param after
     * @return
     */
    public static double secondsBetween(ZonedDateTime before, ZonedDateTime after){
        Duration duration =  Duration.between(before, after);
        double seconds = TimeUnit.NANOSECONDS.toSeconds(duration.getNano());
        return seconds;
    }

    /**
     *method for hours conversion
     * @param hourKey
     * @return
     */
    public static int getNextHourKeyInt(int hourKey) {
        long hourKeyLong = (long) hourKey * 10000000L;
        ZonedDateTime dt = OrdinalMillisToDateTime(hourKeyLong, true).plusHours(1);
        return DateTimeToHourKeyInt(dt);
    }
}
