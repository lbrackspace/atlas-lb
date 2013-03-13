package org.openstack.atlas.util.converters;

import org.openstack.atlas.util.common.exceptions.ConverterException;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;


public class DateTimeConverters {
    private static final Pattern tzPattern = Pattern.compile(".*(\\+|\\-)([0-9][0-9]:[0-9][0-9])$");

    public static Calendar isoTocalNoExc(String isoStr) {
        Calendar cal = null;
        try{
            cal = isoTocal(isoStr);
        }catch(ConverterException ex) {
            return null;
        }
        return cal;
    }

    public static Calendar isoTocal(String isoStr) throws ConverterException {
        Calendar out;
        Matcher tzMatcher;
        String tzStr;
        DateTimeZone dtZone;
        if(isoStr == null){
            throw new ConverterException(new NullPointerException());
        }
        try {
            tzMatcher = tzPattern.matcher(isoStr);
            if(tzMatcher.find()) {
                tzStr = String.format("%s%s",tzMatcher.group(1),tzMatcher.group(2));
                dtZone = DateTimeZone.forID(tzStr);
            }else{
                dtZone = DateTimeZone.UTC;
            }
            DateTime dateTime = new DateTime(isoStr,dtZone);
            out = dateTime.toCalendar(Locale.ROOT);
        } catch (Exception ex) {
            throw new ConverterException(ex);
        }
        return out;
    }

    public static String calToiso(Calendar cal) throws ConverterException {
        String out;
        String msg;
        try {
            DateTime dateTime = new DateTime(cal);
            out = ISODateTimeFormat.dateTimeNoMillis().print(dateTime);
        } catch (Exception ex) {
            throw new ConverterException(ex);
        }

        return out;
    }

    public static String calToisoNoExc(Calendar cal) {
        String isoStr= null;
        try{
            isoStr = calToiso(cal);
        }catch(ConverterException ex) {
            return null;
        }
        return isoStr;
    }
}
