package org.openstack.atlas.hadoop.deprecated;

import java.util.*;

@Deprecated
public class DateTime implements Comparable<DateTime> {

    public static final int ISO = 1;
    public static final int ISO_DATE = 2;
    public static final int APACHE = 3;
    public static final int RUNTIME = 4;

    private static final Map<String, Integer> MONTH_MAP = new HashMap<String, Integer>();

    private String dates[];

    private Calendar dumbCalendar;

    private String times[];

    private int year;

    private int month;

    private int day;

    private int hour;

    private int minute;

    private int second;

    private int millis = 0;

    public DateTime() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("America/Chicago"));
        dumbCalendar = c;
        refreshDates();
    }

    public DateTime(Calendar c) {
        this.dumbCalendar = c;
        refreshDates();
    }

    public DateTime(Date date) {
        dumbCalendar = Calendar.getInstance(TimeZone.getTimeZone("America/Chicago"));
        dumbCalendar.setTime(date);
        refreshDates();
    }

    public DateTime(String iso) {
        this(iso, DateTime.ISO);
    }

    public DateTime(String date, int format) {
        dumbCalendar = Calendar.getInstance(TimeZone.getTimeZone("America/Chicago"));
        populateValuesFromFormatString(date, format);
        dumbCalendar.set(year, month, day, hour, minute, second);
        dumbCalendar.set(Calendar.MILLISECOND, millis);
    }

    static {
        MONTH_MAP.put("JAN", 1);
        MONTH_MAP.put("FEB", 2);
        MONTH_MAP.put("MAR", 3);
        MONTH_MAP.put("APR", 4);
        MONTH_MAP.put("MAY", 5);
        MONTH_MAP.put("JUN", 6);
        MONTH_MAP.put("JUL", 7);
        MONTH_MAP.put("AUG", 8);
        MONTH_MAP.put("SEP", 9);
        MONTH_MAP.put("OCT", 10);
        MONTH_MAP.put("NOV", 11);
        MONTH_MAP.put("DEC", 12);
    }

    public static int getNumericalMonth(String month) {
        Integer monthInt = MONTH_MAP.get(month.toUpperCase());
        if (monthInt == null) {
            return 0;
        } else {
            return monthInt;
        }
    }

    public static String getMonthStringSymbol(Integer month) {
        for (Map.Entry<String, Integer> value : MONTH_MAP.entrySet()) {
            if (value.getValue() == month.intValue()) {
                String capsMo = value.getKey();
                return capsMo.substring(0, 1) + capsMo.substring(1).toLowerCase();
            }
        }

        return "";

    }

    public static String prependZero(int i) {
        String s = Integer.toString(i);
        return (s.length() == 1) ? "0" + s : "" + s;
    }

    public int compareTo(DateTime arg0) {
        return dumbCalendar.compareTo(arg0.getCalendar());
    }

    public Calendar getCalendar() {
        return dumbCalendar;
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public String getIso() {
        return year + "-" + prependZero(month + 1) + "-" + prependZero(day) + " " + prependZero(hour) + ":"
                + prependZero(minute) + ":" + prependZero(second);
    }

    public String getApache() {
        return prependZero(day) + "/" + getMonthStringSymbol(month + 1) + "/" + year + ":" + prependZero(hour) + ":"
                + prependZero(minute) + ":" + prependZero(second);
    }

    public int getMinute() {
        return minute;
    }

    public int getMonth() {
        return month;
    }

    public int getSecond() {
        return second;
    }

    public int getYear() {
        return year;
    }

    public void roll(int field, int amount) {
        dumbCalendar.add(field, amount);
        refreshDates();
    }

    public void setDay(int day) {
        dumbCalendar.set(Calendar.DAY_OF_MONTH, day);
        this.day = day;
    }

    public void setHour(int hour) {
        dumbCalendar.set(Calendar.HOUR_OF_DAY, hour);
        this.hour = hour;
    }

    public void setMinute(int minute) {
        dumbCalendar.set(Calendar.MINUTE, minute);
        this.minute = minute;
    }

    public void setMonth(int month) {
        dumbCalendar.set(Calendar.MONTH, month - 1);
        this.month = month - 1;
    }

    public void setSecond(int second) {
        dumbCalendar.set(Calendar.SECOND, second);
        this.second = second;
    }

    public void setYear(int year) {
        dumbCalendar.set(Calendar.YEAR, year);
        this.year = year;
    }

    @Override
    public String toString() {
        return getIso();
    }

    private void populateValuesFromFormatString(String date, int format) {
        switch (format) {
            case DateTime.ISO:
                dates = date.split(" ", 2)[0].split("-", 3);
                times = date.split(" ", 2)[1].split(":", 3);

                year = Integer.parseInt(dates[0]);
                month = Integer.parseInt(dates[1]) - 1;
                day = Integer.parseInt(dates[2]);

                hour = Integer.parseInt(times[0]);
                minute = Integer.parseInt(times[1]);
                second = Integer.parseInt(times[2]);
                break;
            case DateTime.ISO_DATE:
                dates = date.split(" ", 2)[0].split("-", 3);
                year = Integer.parseInt(dates[0]);
                month = Integer.parseInt(dates[1]) - 1;
                day = Integer.parseInt(dates[2]);
                break;
            case DateTime.APACHE:
                dates = date.split(":", 2)[0].split("/", 3);
                times = date.split(":", 2)[1].split(":", 3);

                year = Integer.parseInt(dates[2]);
                month = DateTime.getNumericalMonth(dates[1]) - 1;
                day = Integer.parseInt(dates[0]);

                hour = Integer.parseInt(times[0]);
                minute = Integer.parseInt(times[1]);
                second = Integer.parseInt(times[2]);
                break;
            case DateTime.RUNTIME:
                //20100528-144250
                String[] items = date.split("-");
                year = Integer.parseInt(items[0].substring(0, 4));
                month = Integer.parseInt(items[0].substring(4, 6)) - 1;
                day = Integer.parseInt(items[0].substring(6, 8));

                hour = Integer.parseInt(items[1].substring(0, 2));
                minute = Integer.parseInt(items[1].substring(2, 4));
                second = Integer.parseInt(items[1].substring(4, 6));
                break;
            default:
                break;
        }
    }

    private void refreshDates() {
        year = dumbCalendar.get(Calendar.YEAR);
        month = dumbCalendar.get(Calendar.MONTH);
        day = dumbCalendar.get(Calendar.DAY_OF_MONTH);
        hour = dumbCalendar.get(Calendar.HOUR_OF_DAY);
        minute = dumbCalendar.get(Calendar.MINUTE);
        second = dumbCalendar.get(Calendar.SECOND);
    }
}