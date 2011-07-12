package org.openstack.atlas.usage.helpers;

import java.util.Calendar;
import java.util.TimeZone;

public final class TimeZoneHelper {
    
    public static Calendar getCalendarForTimeZone(Calendar endTime, TimeZone timeZone) {
        Calendar cal = (Calendar) endTime.clone();
        cal.setTimeZone(timeZone);
        return cal;
    }
}
