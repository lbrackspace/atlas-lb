package org.openstack.atlas.logs.hadoop.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class DateHelper {
    // 02/May/2009:02:35:30
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss");

    private static final SimpleDateFormat ON_HOUR_FORMAT = new SimpleDateFormat("dd/MMM/yyyy:HH");

    private DateHelper() {

    }

    public static Date parseDate(String date) throws ParseException {
        return DATE_FORMAT.parse(date);
    }

    public static Date parseOnHourDate(String date) throws ParseException {
        return ON_HOUR_FORMAT.parse(date);
    }
}
