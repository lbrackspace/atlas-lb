package org.openstack.atlas.util.converters;

import org.openstack.atlas.util.common.exceptions.ConverterException;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
        ZoneOffset dtZone;
        if(isoStr == null){
            throw new ConverterException(new NullPointerException());
        }
        try {
            tzMatcher = tzPattern.matcher(isoStr);
            if(tzMatcher.find()) {
                tzStr = String.format("%s%s",tzMatcher.group(1),tzMatcher.group(2));
                dtZone = ZoneOffset.of(tzStr);
            }else{
                dtZone = ZoneOffset.UTC;
            }
            OffsetDateTime dateTime =  OffsetDateTime.of(LocalDateTime.parse(isoStr, DateTimeFormatter.ISO_DATE_TIME),dtZone);
            out = GregorianCalendar.from(ZonedDateTime.from(dateTime));
        } catch (Exception ex) {
            throw new ConverterException(ex);
        }
        return out;
    }

    public static String calToiso(Calendar cal) throws ConverterException {
        String out;
        String msg;
        try {
            ZonedDateTime dateTime =ZonedDateTime.ofInstant(cal.toInstant(),ZoneOffset.UTC);
            DateTimeFormatter dtf = DateTimeFormatter.ISO_INSTANT;
            out =  dtf.format(dateTime.withNano(0));
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
