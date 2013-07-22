package org.rackspace.capman.tools.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rackspace.capman.tools.ca.primitives.RsaConst;

public class StaticHelpers {

    public static final long DAY_IN_MILLIS_LONG = (long) 24 * 60 * 60 * 1000;
    private static final String[] int2hex;

    static {
        int j;
        RsaConst.init();
        byte[] oneByte = new byte[1];
        int2hex = new String[16];
        j = 48;
        for (int i = 0; i < 10; i++, j++) {
            oneByte[0] = (byte) j;
            try {
                int2hex[i] = new String(oneByte, "us-ascii");
            } catch (UnsupportedEncodingException ex) {
                int2hex[i] = "*";
            }
        }
        j = 97;
        for (int i = 10; i < 16; i++, j++) {
            oneByte[0] = (byte) j;
            try {
                int2hex[i] = new String(oneByte, "us-ascii");
            } catch (UnsupportedEncodingException ex) {
                int2hex[i] = "*";
            }
        }
    }

    public static String[] getHexMap() {
        String[] mapOut = new String[16];
        for (int i = 0; i < 16; i++) {
            mapOut[i] = int2hex[i];
        }
        return mapOut;
    }

    public static BigInteger string2BigInt(String in) throws UnsupportedEncodingException {
        byte[] strBytes = in.getBytes("UTF-8");
        return bytes2BigInt(strBytes);
    }

    public static BigInteger bytes2BigInt(byte[] in) {
        BigInteger out = BigInteger.ZERO;
        for (int i = 0; i < in.length; i++) {
            out = out.shiftLeft(8).add(BigInteger.valueOf(uint(in[i])));
        }
        return out;
    }

    public static String bytes2hex(byte[] in) {
        StringBuilder sb = new StringBuilder();
        if (in == null) {
            return null;
        }
        for (int i = 0; i < in.length; i++) {
            int byteInt = (in[i] >= 0) ? (int) in[i] : (int) in[i] + 256;
            sb.append(int2hex[byteInt >> 4]); // High nibble
            sb.append(int2hex[byteInt & 0x0f]); // Low nibble
        }
        String out = sb.toString();
        return out;
    }

    // Cause jython has a hard time building byte arrays
    public static byte[] string2bytes(String in) throws UnsupportedEncodingException {
        byte[] out = in.getBytes("UTF-8");
        return out;
    }

    private static int uint(byte in) {
        return (in >= 0) ? (int) in : (int) in + 256;
    }

    // Does nothing Useful. Just a doorstop for debuggin
    private int nop(int in) {
        byte inByte = (byte) (in % 256);
        int out = uint(inByte);
        return out;
    }

    // Cause I keep forget what a Set operations really look like
    public static <U> Set<U> andSet(Set<U> a, Set<U> b) {
        Set<U> aCopy = new HashSet<U>(a);
        Set<U> bCopy = new HashSet<U>(b);
        aCopy.retainAll(bCopy);
        return aCopy;
    }

    public static <U> Set<U> orSet(Set<U> a, Set<U> b) {
        Set<U> aCopy = new HashSet<U>(a);
        Set<U> bCopy = new HashSet<U>(b);
        aCopy.addAll(bCopy);
        return aCopy;
    }

    // Also known as the asymetric difference of 2 sets
    public static <U> Set<U> subtractSet(Set<U> a, Set<U> b) {
        Set<U> aCopy = new HashSet<U>(a);
        Set<U> bCopy = new HashSet<U>(b);
        aCopy.removeAll(b);
        return aCopy;
    }

    // Also known as the symetric difference between sets
    public static <U> Set<U> xorSet(Set<U> a, Set<U> b) {
        Set<U> aCopy = new HashSet<U>(a);
        Set<U> bCopy = new HashSet<U>(b);

        Set<U> intersection = new HashSet<U>();
        Set<U> union = new HashSet<U>();
        intersection.addAll(aCopy);
        intersection.retainAll(bCopy);
        union.addAll(aCopy);
        union.addAll(bCopy);
        union.removeAll(intersection);
        return union;
    }

    // Shortcut to fetching the object stored in a single element Set
    public static <U> Object getFirst(Set<U> a) {
        List<U> aList = new ArrayList(a);
        if (aList.size() < 1) {
            return null;
        }
        return aList.get(0);
    }

    public static Date calendarToDate(Calendar cal) {
        return cal.getTime();
    }

    public static Calendar dateToCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());
        return cal;
    }

    public static Calendar utcCalendarFromTuple(int... tup) {
        Calendar cal;
        TimeZone utc;
        int year = (tup.length > 0) ? tup[0] : 0;
        int month = (tup.length > 1) ? tup[1] - 1 : 0;
        int day = (tup.length > 2) ? tup[2] : 0;
        int hour = (tup.length > 3) ? tup[3] : 0;
        int min = (tup.length > 4) ? tup[4] : 0;
        int sec = (tup.length > 5) ? tup[5] : 0;
        long ms = ((tup.length > 6) ? tup[6] : 0) % 1000;
        utc = TimeZone.getTimeZone("GMT");
        cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeZone(utc);
        cal.set(year, month, day, hour, min, sec);

        long calInMillis = cal.getTimeInMillis();
        cal.setTimeInMillis(calInMillis + ms);
        return cal;
    }

    public static Date dateFromTuple(int... tup) {
        return utcCalendarFromTuple(tup).getTime();
    }

    public static String getCalendarString(Calendar cal) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSSS");
        SimpleDateFormat timeFormat = new SimpleDateFormat("z");

        String dateStr = dateFormat.format(cal.getTime());
        String zoneStr = timeFormat.format(cal.getTime());
        String readableString = String.format("%s %s", dateStr, zoneStr);
        return readableString;
    }

    public static String getDateString(Date date) {
        return getCalendarString(dateToCalendar(date));
    }

    public static Date currDate() {
        return new Date(System.currentTimeMillis());
    }

    public static List<Throwable> getExceptionCausesList(Throwable th) {
        List<Throwable> causes = new ArrayList<Throwable>();
        Throwable t;
        t = th;
        while (t != null) {
            causes.add(t);
            t = t.getCause();
        }
        return causes;
    }

    public static int ubyte2int(byte in) {
        return (in >= 0) ? (int) in : (int) in + 256;
    }

    public static byte int2ubyte(int in) {
        in &= 0xff;
        return (in < 128) ? (byte) in : (byte) (in - 256);
    }

    public static boolean isByteWhiteSpace(byte byteIn) {
        int ch;
        ch = ubyte2int(byteIn);
        return (ch >= 0x09 && ch <= 0x0d) || (ch >= 0x1c && ch <= 0x20);
    }

    public static List<Object> filterObjectList(List<Object> objsIn, Set<Class> clazz) {
        List<Object> objsOut = new ArrayList<Object>();
        for (Object obj : objsIn) {
            if (obj == null) {
                continue;
            }
            if (clazz != null && !clazz.contains(obj.getClass())) {
                continue;
            }
            objsOut.add(obj);
        }
        return objsOut;
    }

    public static Date daysDelta(Date date, int days) {
        return new Date(date.getTime() + (long) days * DAY_IN_MILLIS_LONG);
    }

    public static Date daysDelta(Date date, double days) {
        return new Date((long) ((double) date.getTime() + days * DAY_IN_MILLIS_LONG));
    }

    public static Date now() {
        return new Date(System.currentTimeMillis());
    }
}
